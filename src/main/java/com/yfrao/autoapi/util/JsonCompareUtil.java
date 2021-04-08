package com.yfrao.autoapi.util;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;

@Slf4j
public class JsonCompareUtil {
    private final static String FAIL_RESULT = "结果：fail  ";
    private final static String STRING_NULL = "null";
    private final static String SUCCESS_RESULT = "success";
    private final static String MAP_JSON_OBJECT = "json.JSONObject";
    private final static String LIST_JSON_ARRAY = "json.JSONArray";
    private final static String LIST_RESULT = "list中未匹配到一致项";

    /**
     * 数据检查
     *
     * @param response 接口返回结果
     * @param expect   预期结果
     * @return 返回数据检查结果
     */
    private static String checkData(String response, String expect) {

        if (StringUtils.isEmpty(response)) {
            return "接口返回结果为空";
        }

        /*接口返回结果转json*/
        try {
            JSONObject.fromObject(response);
        } catch (Exception e) {
            return "接口返回结果转json异常:" + response + FAIL_RESULT;
        }

        /*预期结果转json*/
        try {
            JSONObject.fromObject(expect);
        } catch (Exception e) {
            return "预期结果转json异常:" + expect + FAIL_RESULT;
        }

        return SUCCESS_RESULT;
    }


    /**
     * 检查list数据
     *
     * @param response 接口返回
     * @param expect   预期结果
     * @param key      对应的key
     * @return 检查结果
     * @throws JSONException json转换异常
     */
    private static String listDataCheck(String response, String expect, String key) throws JSONException {
        StringBuilder listCheckResult = new StringBuilder();
        //1、接口返回list为空,结果返回null：接口返回为null
        if (StringUtils.isEmpty(response) || StringUtils.equals(STRING_NULL, response)) {
            //记录key
            listCheckResult.append("\r\n").append(key);
            listCheckResult.append("\r\n").append("  预期：").append(expect);
            listCheckResult.append("\r\n").append("  实际：").append(response);
            listCheckResult.append("\r\n").append("  结果：").append("fail  ");
            return listCheckResult.toString();
        }

        //2、转换为jsonArray
        JSONArray exceptList = new JSONArray(expect);
        JSONArray responseList = new JSONArray(response);

        //3、 发现预期结果list有三个元素，实际接口只有2个元素情况，导致校验异常=>增加list元素判断
        int responseSize = responseList.length();
        int exceptSize = exceptList.length();

        //3.1、 预期size大于实际size
        if (exceptSize > responseSize) {
            //记录key
            listCheckResult.append("\r\n").append(key).append(":list元素预期数大于实际数");
            listCheckResult.append("\r\n").append("  预期size：").append(exceptSize);
            listCheckResult.append("\r\n").append("  实际size：").append(responseSize);
            listCheckResult.append("\r\n").append("  结果：").append("fail  ");
            return listCheckResult.toString();
        }

        //3.2、 预期为空，接口返回不为空，建议在预期里去掉这个预期
        if (exceptSize == 0 && responseSize != 0) {
            //记录key
            listCheckResult.append("\r\n").append(key);
            listCheckResult.append("\r\n").append("  预期list为空，实际接口不为空，建议在预期中去除该字段！");
            listCheckResult.append("\r\n").append("  结果：").append("fail  ");
            return listCheckResult.toString();
        }

        return SUCCESS_RESULT;

    }


    /**
     * list校验对比
     *
     * @param response 返回结果
     * @param expect   预期结果
     * @param key      结果对应的key
     * @return 比对结果
     */
    private static String listCompare(String response, String expect, String key) throws JSONException {

        StringBuilder listResult = new StringBuilder();
        //1、检查list数据
        if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, listDataCheck(response, expect, key))) {
            return listDataCheck(response, expect, key);
        }
        //2、转换为jsonArray
        JSONArray exceptList = new JSONArray(expect);
        JSONArray responseList = new JSONArray(response);
        //3、判断list元素为map,遍历
        if (isMap(exceptList.get(0))) {
            int size = exceptList.length();
            for (int i = 0; i < size; i++) {
                String tempListResult = jsonCompare(responseList.get(i).toString(), exceptList.get(i).toString());
                //结果判断：fail 拼接路径和结果
                if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, tempListResult)) {
                    for (String compareListResult : tempListResult.split(FAIL_RESULT)) {
                        listResult.append("\r\n").append(key).append("[").append(i).append("].").append(compareListResult.substring(2)).append(FAIL_RESULT);
                    }
                }
            }
        } else {
            //4、不是map直接聚合结果
            listResult.append("\r\n").append(key);
            listResult.append("\r\n").append("  预期：").append(expect);
            listResult.append("\r\n").append("  实际：").append(StringUtils.isEmpty(response) ? STRING_NULL : response);
            listResult.append("\r\n").append("  结果：").append("fail  ");
        }

        //5、没有失败结果写入success
        if (StringUtils.isEmpty(listResult.toString())) {
            listResult.append(SUCCESS_RESULT);
        }
        return listResult.toString();

    }


    /**
     * 忽略排序的list对比
     * @param response 返回结果
     * @param expect 预期结果
     * @param key 对比的字段
     * @return 对比结果
     * @throws JSONException
     */
    private static String listCompareIgnoreSort(String response, String expect, String key) throws JSONException {

        StringBuilder listResult = new StringBuilder();
        //1、检查list数据
        if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, listDataCheck(response, expect, key))) {
            return listDataCheck(response, expect, key);
        }
        //2、转换为jsonArray
        JSONArray exceptList = new JSONArray(expect);
        JSONArray responseList = new JSONArray(response);
        //3、判断list元素为map,遍历
        String tempListResult=null;
        if (isMap(exceptList.get(0))) {
            int iSize = exceptList.length();
            int jSize =responseList.length();
            for (int i = 0; i < iSize; i++) {
                for(int j=0;j<jSize;j++){
                    tempListResult=jsonCompare(responseList.get(j).toString(), exceptList.get(i).toString());
                    if (StringUtils.equalsIgnoreCase(SUCCESS_RESULT, tempListResult)){
                        log.info(key+"：预期list["+i+"] = 返回list["+j+"]");
                        break;
                    }
                    log.warn(key+"：预期list["+i+"] != 返回list["+j+"]");
                }
                //结果判断：走到这里说明一轮遍历结束，如果不是success,说明未匹配到一致的list
                if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, tempListResult)) {
                    for (String compareListResult : tempListResult.split(FAIL_RESULT)) {
                        listResult.append("\r\n").append(key).append("[").append(i).append("].").append(compareListResult.substring(2)).append(LIST_RESULT).append(FAIL_RESULT);
                    }
                }
            }
        } else {
            //4、不是map直接聚合结果
            listResult.append("\r\n").append(key);
            listResult.append("\r\n").append("  预期：").append(expect);
            listResult.append("\r\n").append("  实际：").append(StringUtils.isEmpty(response) ? STRING_NULL : response);
            listResult.append("\r\n").append("  结果：").append("fail  ");
        }

        //5、没有失败结果写入success
        if (StringUtils.isEmpty(listResult.toString())) {
            listResult.append(SUCCESS_RESULT);
        }
        return listResult.toString();

    }



    /**
     * map校验对比
     *
     * @param response 返回结果
     * @param expect   预期结果
     * @param key      值对应的key
     * @return 比对结果
     */
    private static String mapCompare(String response, String expect, String key) {
        StringBuilder mapResult = new StringBuilder();
        //接口返回map为空,结果返回null：接口返回为null（判断"null"，因为部分json工具类把null直接转成了"null"）
        if (StringUtils.isEmpty(response) || StringUtils.equals(STRING_NULL, response)) {
            //记录key
            mapResult.append("\r\n").append(key);
            mapResult.append("\r\n").append("  预期：").append(expect);
            mapResult.append("\r\n").append("  实际：").append(response);
            mapResult.append("\r\n").append("  结果：").append("fail  ");

        } else {
            //返回map对比的结果:1、success:pass,2、fail拼接路径和结果
            String tempResult = jsonCompare(response, expect);

            if (StringUtils.equalsIgnoreCase(SUCCESS_RESULT, tempResult)) {
                mapResult.append(SUCCESS_RESULT);
            } else {
                for (String compareResult : tempResult.split(FAIL_RESULT)) {
                    mapResult.append("\r\n").append(key).append(".").append(compareResult.substring(2)).append(FAIL_RESULT);
                }
            }
        }

        return mapResult.toString();
    }


    /**
     * json对比实现方法
     *
     * @param response 接口返回结果
     * @param expect   预期结果
     * @return 对比校验结果
     */
    public static String jsonCompare(String response, String expect) {

        //0、如果是list直接走list对比
        StringBuilder result = new StringBuilder();
        if (isList(expect)) {
            try {
                result.append(listCompareIgnoreSort(response, expect, ""));
            } catch (JSONException e) {
                result.append("  接口返回：").append(response);
                result.append("  预期结果：").append(expect);
                result.append("  对比结果：").append("异常:").append(e.getMessage());
            }
        } else {
            //1、校验数据
            if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, checkData(response, expect))) {
                return checkData(response, expect);
            }

            //2、转json对象
            JSONObject responseObject = JSONObject.fromObject(response);
            JSONObject expectObject = JSONObject.fromObject(expect);

            //3、遍历对比
            try {
                for (Object key : expectObject.keySet()) {

                    //4、接口返回无预期key
                    if (! responseObject.containsKey(key)) {
                        result.append("\r\n").append(key).append("：接口未返回 结果：fail  ");
                    } else {
                        //5、预期与实际不一致
                        if (! StringUtils.equals(responseObject.getString((String) key), expectObject.getString((String) key))) {
                            //6、判断是map
                            if (isMap(expectObject.get(key))) {
                                if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, mapCompare(responseObject.getString((String) key), expectObject.getString((String) key), (String) key))) {
                                    result.append(mapCompare(responseObject.getString((String) key), expectObject.getString((String) key), (String) key));
                                }
                                // 7、是list
                            } else if (isList(expectObject.get(key))) {
                                if (! StringUtils.equalsIgnoreCase(SUCCESS_RESULT, listCompareIgnoreSort(responseObject.getString((String) key), expectObject.getString((String) key), (String) key))) {
                                    result.append(listCompare(responseObject.getString((String) key), expectObject.getString((String) key), (String) key));
                                }

                            } else {
                                //8、一般的key:value
                                result.append("\r\n").append(key);
                                result.append("\r\n").append("  预期：").append(expectObject.getString((String) key));
                                result.append("\r\n").append("  实际：").append(StringUtils.isEmpty(responseObject.getString((String) key)) ? STRING_NULL : responseObject.getString((String) key));
                                result.append("\r\n").append("  结果：").append("fail  ");
                            }
                        }
                    }
                }

            } catch (Exception e) {
                result.append("  接口返回：").append(response);
                result.append("  预期结果：").append(expect);
                result.append("  对比结果：").append("异常:").append(e.getMessage());
            }
        }
        if (result.toString().isEmpty()) {
            result.append(SUCCESS_RESULT);
        }
        return result.toString();
    }


    /**
     * 根据class判断对象是否是map
     *
     * @param object 校验对象
     * @return 是否是map
     */
    @SuppressWarnings ("unchecked")
    private static Boolean isMap(Object object) {
        boolean isMap = false;
        if (object.getClass().toString().contains(MAP_JSON_OBJECT)) {
            try {
                JSONObject mapObject = JSONObject.fromObject(object);
                Map<String, Object> objectMap = (Map<String, Object>) mapObject;
                isMap = true;
            } catch (Exception e) {
                return false;
            }
        }
        return isMap;
    }

    /**
     * 根据class判断对象是否是list
     *
     * @param object 校验对象
     * @return 是否是list
     */
    private static Boolean isList(Object object) {
        boolean isList = false;
        /*新增string是否是list的判断*/
        if (object instanceof String) {
            try {
                new JSONArray(object.toString());
                isList = true;
            } catch (JSONException e) {
                return false;
            }
        }
        if (object.getClass().toString().contains(LIST_JSON_ARRAY)) {
            isList = true;
        }
        return isList;
    }


    public static void main(String[] args) {

        String response = "{\"success\":true,\"listMap\":[{\"A\":\"\",\"B\":\"bb\",\"C\":3},{\"A2\":\"aa2\",\"B2\":\"bb2\",\"C2\":32}],\"list\":[\"test1\",\"test2\",\"test3\",\"test4\"],\"map\":{\"mapA\":\"aa\",\"mapB\":\"bb\",\"mapC\":3,\"mapD\":[1,2,3,4],\"mapE\":{\"test1\":11,\"test2\":22}},\"code\":\"123\",\"message\":\"33\"}";
        String responsenull = null;
        String expect = "{\"success\":true,\"listMap\":[{\"A\":\"aa_error\",\"B\":\"bb_error\",\"C\":3},{\"A2\":\"aa2\",\"B2\":\"bb2_error\",\"C2\":32}],\"list\":[\"test1_error\",\"test2\",\"test3\",\"test4\"],\"map\":{\"mapAB\":\"aa_error\",\"mapB\":\"bb\",\"mapC\":3,\"mapD\":[2,2,3,4],\"mapE\":{\"test1\":112,\"test2\":222}},\"code\":\"123\",\"message1\":\"331\"}";
        String response1 = "{\"regionId\":\"7865\",\"officeId\":\"8346\",\"dealerId\":\"610038\",\"channelType\":\"2\",\"chainType\":\"\",\"chainContent\":\"1\",\"routes\":{},\"routeIds\":[]}";
        String expect1 = "{\"regionId\":\"7865\",\"officeId\":\"8346\",\"dealerId\":\"610038\",\"channelType\":\"2\",\"chainType\":\"\",\"custState\":\"1\",\"chainContent\":\"1\",\"routes\":{},\"routeIds\":[]}";
        String response3 = "{\"msg\":null,\"code\":null,\"data\":{\"loginId\":\"xwcai\",\"totalPage\":1,\"pageSize\":10,\"orderRecordVOS\":[{\"orderType\":1,\"supplierId\":1,\"orderReasonDesc\":\"标准\",\"modifiedUser\":\"system\",\"receiveContactPhone\":\"14169247884\",\"wayBillVOS\":[{\"grossWeight\":1.000,\"statusDesc\":\"已收货\",\"carrierName\":\"自备车\",\"expressNo\":\"2018122109260510015\",\"freight\":null,\"wayBillNo\":\"yd-2018122109260510015\",\"grossWeightStr\":\"1.000KG\",\"departureDate\":\"2018-12-21 09:26:59\",\"carrierId\":538,\"status\":3,\"weightUnit\":\"KG\",\"createDate\":\"2018-12-21 09:26:05\"}],\"remark\":\"备注文本\",\"buyerId\":305878,\"receiveRegion\":\"西湖区\",\"consigneeName\":\"上海记杰商贸有限公司\",\"deatailPageVO\":{\"loginId\":\"xwcai\",\"totalPage\":1,\"pageSize\":10,\"currentPage\":1,\"detailVOS\":[{\"orderNo\":\"rice20181221muUBfG\",\"receivedAmount\":1,\"netWeightStr\":\"1.000KG\",\"sendAmount\":1,\"skuName\":\"500g装大米天地盖纸箱\",\"grossWeight\":1.000,\"netWeight\":1.000,\"requiredAmount\":1,\"skuNo\":\"14110102125\",\"grossWeightStr\":\"1.000KG\",\"skuUnit\":\"ZHI\",\"skuId\":10528,\"dispatchAmount\":1,\"weightUnit\":\"KG\",\"status\":1}],\"currentSystemType\":4,\"totalElements\":1},\"orderReason\":0,\"receiveContactName\":\"张三\",\"consigneeId\":305878,\"createDate\":\"2018-12-21 09:25:35\",\"supplierName\":\"肇源基地\",\"orderNo\":\"rice20181221muUBfG\",\"statusDesc\":\"已收货\",\"address\":\"浙江省/杭州市/西湖区/葛衙庄181号\",\"consigneeType\":3,\"receiveAddress\":\"葛衙庄181号\",\"buyerName\":\"上海记杰商贸有限公司\",\"orderTypeDesc\":\"销售订单\",\"receiveProvince\":\"浙江省\",\"receiveCity\":\"杭州市\",\"buyerType\":3,\"supplierType\":6,\"status\":40}],\"currentPage\":1,\"currentSystemType\":4,\"totalElements\":1},\"success\":true}";
        String expect3 = "{\"msg\":null,\"code\":null,\"data\":{\"loginId\":\"xwcai\",\"totalPage\":1,\"pageSize\":10,\"orderRecordVOS\":[{\"orderType\":1,\"supplierId\":1,\"orderReasonDesc\":\"标准\",\"modifiedUser\":\"system\",\"receiveContactPhone\":\"14169247884\",\"wayBillVOS\":[{\"grossWeight\":1.000,\"statusDesc\":\"已收货\",\"carrierName\":\"自备车\",\"expressNo\":\"2018122109260510015\",\"freight\":null,\"grossWeightStr\":\"1.000KG\",\"departureDate\":\"2018-12-21 09:26:59\",\"carrierId\":538,\"status\":3,\"weightUnit\":\"KG\",\"createDate\":\"2018-12-21 09:26:05\"}],\"remark\":\"备注文本\",\"buyerId\":305878,\"receiveRegion\":\"西湖区\",\"consigneeName\":\"上海记杰商贸有限公司\",\"deatailPageVO\":{\"loginId\":\"xwcai\",\"totalPage\":1,\"pageSize\":10,\"currentPage\":1,\"detailVOS\":[{\"orderNo\":\"rice20181221muUBfG\",\"receivedAmount\":1,\"netWeightStr\":\"1.000KG\",\"sendAmount\":1,\"skuName\":\"500g装大米天地盖纸箱\",\"grossWeight\":1.000,\"netWeight\":1.000,\"requiredAmount\":1,\"skuNo\":\"14110102125\",\"grossWeightStr\":\"1.000KG\",\"skuUnit\":\"ZHI\",\"skuId\":10528,\"dispatchAmount\":1,\"weightUnit\":\"KG\",\"status\":1}],\"currentSystemType\":4,\"totalElements\":1},\"orderReason\":0,\"receiveContactName\":\"张三\",\"consigneeId\":305878,\"createDate\":\"2018-12-21 09:25:35\",\"supplierName\":\"肇源基地\",\"orderNo\":\"rice20181221muUBfG\",\"statusDesc\":\"已收货\",\"address\":\"浙江省/杭州市/西湖区/葛衙庄181号\",\"consigneeType\":3,\"receiveAddress\":\"葛衙庄181号\",\"buyerName\":\"上海记杰商贸有限公司\",\"orderTypeDesc\":\"销售订单\",\"receiveProvince\":\"浙江省\",\"receiveCity\":\"杭州市\",\"buyerType\":3,\"supplierType\":6,\"status\":40}],\"currentPage\":1,\"currentSystemType\":4,\"totalElements\":1},\"success\":true}";
        String expect2 = "{ \"msg\": \"操作成功\", \"code\": \"200\", \"data\": [ { \"code\": \"ZY19064400000031ZY1745T\", \"taskNo\": \"9064_ZY1000945-20190109161923\", \"skuNo\": \"000000011110101797\", \"batchNo\": \"20190110\", \"skuName\": \"农夫山泉-东北大米-6*0.5KG*6袋-纸箱装\", \"skuId\": 14882 } ], \"success\": true }";
        String response2 = "{\"msg\":\"二维码不正确或查询为空 \",\"code\":\"\",\"data\":null,\"success\":false}";
        String expect4 = "{ \"msg\": null, \"code\": null, \"data\": { \"pageDatas\": [ { \"id\": 13295235, \"memberId\": 1000425176, \"receiveTime\": \"2019-01-17 00:56:50\", \"couponRuleId\": 8941, \"couponTemplateId\": 1668, \"couponReceiveId\": 382888, \"ruleType\": \"DB\", \"templateType\": \"O\", \"amount\": 18.8, \"discount\": null, \"askAmount\": 18.8, \"title\": \"满减券-18.8元满\", \"subheading\": null, \"description\": \"订单券\", \"couponStatus\": \"E\", \"consumeTime\": null, \"systemType\": \"zfj\", \"orderId\": null, \"orderSystemType\": null, \"province\": null, \"city\": null, \"canton\": null, \"startDate\": \"2019-01-17 00:56:50\", \"endDate\": \"2019-01-20 00:56:50\", \"lockDate\": null, \"createDate\": \"2019-01-17 00:56:50\", \"createUser\": \"51552\", \"modifyDate\": \"2019-01-20 01:00:01\", \"modifyUser\": \"member_job\", \"reduceAmount\": null, \"couponNo\": null, \"couponStatusName\": \"已过期\", \"templateTypeName\": null, \"prodId\": null, \"skuIdAndName\": null, \"orderStatus\": null, \"orderStatusValue\": \"\", \"couponRuleName\": \"1-14日订单购买\" }, { \"id\": 13295228, \"memberId\": 1000425176, \"receiveTime\": \"2019-01-17 00:50:44\", \"couponRuleId\": 8941, \"couponTemplateId\": 1668, \"couponReceiveId\": 382797, \"ruleType\": \"DB\", \"templateType\": \"O\", \"amount\": 18.8, \"discount\": null, \"askAmount\": 18.8, \"title\": \"满减券-18.8元满\", \"subheading\": null, \"description\": \"订单券\", \"couponStatus\": \"U\", \"consumeTime\": \"2019-01-17 00:52:59\", \"systemType\": \"zfj\", \"orderId\": \"NFXH18000011901170052544616\", \"orderSystemType\": \"zfj\", \"province\": null, \"city\": null, \"canton\": null, \"startDate\": \"2019-01-17 00:50:44\", \"endDate\": \"2019-01-20 00:50:44\", \"lockDate\": \"2019-01-17 00:52:59\", \"createDate\": \"2019-01-17 00:50:44\", \"createUser\": \"51552\", \"modifyDate\": \"2019-01-17 00:52:59\", \"modifyUser\": \"51552\", \"reduceAmount\": 18.8, \"couponNo\": null, \"couponStatusName\": \"已使用\", \"templateTypeName\": null, \"prodId\": -1, \"skuIdAndName\": \"-1 null\", \"orderStatus\": \"2\", \"orderStatusValue\": \"待出货\", \"couponRuleName\": \"1-14日订单购买\" }, { \"id\": 13295221, \"memberId\": 1000425176, \"receiveTime\": \"2019-01-17 00:41:16\", \"couponRuleId\": 8815, \"couponTemplateId\": 573, \"couponReceiveId\": null, \"ruleType\": \"PR\", \"templateType\": \"D\", \"amount\": null, \"discount\": 0.5, \"askAmount\": -2, \"title\": \"5L*2桶5折折扣券\", \"subheading\": null, \"description\": \"*折扣券仅限购买5L*2桶水时使兑换券\\n*每个新用户只能参与一次活动； \\n*兑换券仅限在活动网点内的量贩机上使用；\\n*券有效期为领券日起3天，逾期未使用自动失效。\", \"couponStatus\": \"E\", \"consumeTime\": null, \"systemType\": \"zfj\", \"orderId\": null, \"orderSystemType\": null, \"province\": null, \"city\": null, \"canton\": null, \"startDate\": \"2019-01-17 00:41:16\", \"endDate\": \"2019-01-20 00:41:16\", \"lockDate\": null, \"createDate\": \"2019-01-17 00:41:16\", \"createUser\": \"system\", \"modifyDate\": \"2019-01-20 00:50:01\", \"modifyUser\": \"member_job\", \"reduceAmount\": null, \"couponNo\": null, \"couponStatusName\": \"已过期\", \"templateTypeName\": null, \"prodId\": null, \"skuIdAndName\": null, \"orderStatus\": null, \"orderStatusValue\": \"\", \"couponRuleName\": \"0112支付完成红包\" } ], \"totalElements\": 3, \"currentPage\": 1, \"totalPage\": 1, \"pageSize\": 10 }, \"success\": true }";
        String response4 = "{\"msg\":null,\"code\":null,\"data\":{\"pageDatas\":[{\"templateType\":\"C\",\"couponRuleName\":\"养生堂总部园区3.12抽奖活动\",\"couponRuleId\":10992,\"orderId\":null,\"city\":null,\"endDate\":\"2019-03-14 22:20:55\",\"reduceAmount\":null,\"discount\":null,\"description\":\"*代金券仅限在农夫山泉便民服务点机器上使用；\\n*代金券在购买任意商品时，自动抵扣； \\n*代金券有效期为领券日起3天内，逾期未使用自动失效；\\n*各类优惠券不可叠加使用。\",\"orderStatus\":null,\"prodId\":null,\"title\":\"3元全品类代金券\",\"orderSystemType\":null,\"modifyUser\":null,\"province\":null,\"ruleType\":\"L\",\"couponStatus\":\"N\",\"systemType\":\"zfj\",\"orderStatusValue\":\"\",\"id\":14256566,\"subheading\":null,\"askAmount\":-2.00,\"memberId\":1000425176,\"createDate\":\"2019-03-11 22:20:55\",\"consumeTime\":null,\"couponReceiveId\":null,\"amount\":3.00,\"modifyDate\":\"2019-03-11 22:20:55\",\"couponTemplateId\":1052,\"couponNo\":null,\"templateTypeName\":null,\"receiveTime\":\"2019-03-11 22:20:55\",\"skuIdAndName\":null,\"canton\":null,\"createUser\":null,\"couponStatusName\":\"未使用\",\"startDate\":\"2019-03-11 22:20:55\",\"lockDate\":null},{\"templateType\":\"C\",\"couponRuleName\":\"量贩机西柚茉莉代金券\",\"couponRuleId\":10950,\"orderId\":null,\"city\":null,\"endDate\":\"2019-03-14 22:04:07\",\"reduceAmount\":null,\"discount\":null,\"description\":\"该券仅限在量贩机上购买茶Π 果味茶 西柚茉莉花茶 500ml时自动抵扣\",\"orderStatus\":null,\"prodId\":null,\"title\":\"L西柚茉莉4元代金券\",\"orderSystemType\":null,\"modifyUser\":\"51552\",\"province\":null,\"ruleType\":\"S\",\"couponStatus\":\"N\",\"systemType\":\"zfj\",\"orderStatusValue\":\"\",\"id\":14256559,\"subheading\":null,\"askAmount\":-2.00,\"memberId\":1000425176,\"createDate\":\"2019-03-11 22:04:07\",\"consumeTime\":null,\"couponReceiveId\":1168841,\"amount\":4.00,\"modifyDate\":\"2019-03-11 22:04:07\",\"couponTemplateId\":2004,\"couponNo\":null,\"templateTypeName\":null,\"receiveTime\":\"2019-03-11 22:04:07\",\"skuIdAndName\":null,\"canton\":null,\"createUser\":\"51552\",\"couponStatusName\":\"未使用\",\"startDate\":\"2019-03-11 22:04:07\",\"lockDate\":null}],\"totalPage\":1,\"pageSize\":10,\"currentPage\":1,\"totalElements\":2},\"success\":true}";
        String expect5 = "{ \"msg\":null, \"code\":null, \"data\":{ \"totalElements\":0, \"currentPage\":1, \"totalPage\":0, \"pageSize\":10, \"token\":null, \"timestamp\":null, \"modifyId\":\"124153\", \"orgId\":8334, \"orgIdList\":null, \"from\":null, \"t\":null, \"id\":null, \"name\":null, \"couponTemplateId\":null, \"ruleType\":null, \"createDate\":null, \"startDate\":null, \"endDate\":null, \"status\":2, \"systemType\":null, \"sendType\":null, \"province\":null, \"city\":null, \"canton\":null, \"issueStatus\":null, \"couponRules\":[ ], \"category\":1, \"couponSource\":null }, \"success\":true }";
        String response5 = "{\"msg\":null,\"code\":null,\"data\":{\"couponSource\":null,\"endDate\":null,\"city\":null,\"pageSize\":10,\"orgId\":8334,\"totalElements\":1,\"issueStatus\":null,\"province\":null,\"orgIdList\":null,\"ruleType\":null,\"systemType\":null,\"from\":null,\"id\":null,\"timestamp\":null,\"createDate\":null,\"modifyId\":\"124153\",\"totalPage\":1,\"couponRules\":[{\"prodList\":[],\"issueEndTime\":\"2019-03-14 23:59:59\",\"couponTemplateIds\":[{\"templateType\":\"D\",\"endDate\":null,\"city\":null,\"modifyStatus\":null,\"discount\":0.50,\"description\":null,\"title\":\"量贩机折扣券\",\"modifyUser\":null,\"province\":null,\"systemType\":null,\"id\":2046,\"vaildTimeType\":null,\"subheading\":null,\"askAmount\":null,\"createDate\":null,\"perIssue\":1,\"templateNo\":null,\"amount\":null,\"modifyDate\":null,\"templateTypeName\":\"折扣券\",\"canton\":null,\"createUser\":null,\"vaildTime\":null,\"startDate\":null,\"status\":null}],\"firstText\":\"3-4户限活动\",\"province\":310000,\"newsTitle\":null,\"newsDesc\":null,\"newsPicUrl\":null,\"id\":10768,\"bindCommunity\":0,\"maxIssueCount\":100,\"marketingType\":1,\"modifyDate\":\"2019-03-13 15:04:12\",\"modifyId\":null,\"remarkText\":\"点击入驻领券\",\"newsPicId\":null,\"memberLimit\":1,\"activityUrl\":null,\"createId\":null,\"sendType\":1,\"name\":\"3-4户限活动\",\"purposeList\":[1,2],\"perIssueCount\":null,\"status\":2,\"alterCouponList\":[{\"templateType\":\"D\",\"endDate\":null,\"city\":null,\"modifyStatus\":null,\"discount\":0.50,\"description\":null,\"title\":\"自贩机折扣券\",\"modifyUser\":null,\"province\":null,\"systemType\":null,\"id\":2039,\"vaildTimeType\":null,\"subheading\":null,\"askAmount\":null,\"createDate\":null,\"perIssue\":1,\"templateNo\":null,\"amount\":null,\"modifyDate\":null,\"templateTypeName\":\"折扣券\",\"canton\":null,\"createUser\":null,\"vaildTime\":null,\"startDate\":null,\"status\":null}],\"fileId\":40840,\"cityRange\":[],\"hasAlterCoupon\":1,\"ruleMachineOfficeBean\":{\"officeIds\":[273,362,363,581,898,1443,1445,1446,1447,1449,1494,2926,2927,4761,4768,6224,6338,7885,7970,9155,9156,9157,9226,51180,51182,51183,51185],\"t\":null,\"modifyId\":null,\"orgIdList\":null,\"machineIds\":[{\"machineId\":\"YST18000019\",\"netCommunityId\":null},{\"machineId\":\"YST18000066\",\"netCommunityId\":null},{\"machineId\":\"YST18000143\",\"netCommunityId\":null},{\"machineId\":\"YST18000173\",\"netCommunityId\":null},{\"machineId\":\"YST18000306\",\"netCommunityId\":null},{\"machineId\":\"YST18000326\",\"netCommunityId\":null},{\"machineId\":\"YST18000329\",\"netCommunityId\":null},{\"machineId\":\"YST18000330\",\"netCommunityId\":null},{\"machineId\":\"YST18000331\",\"netCommunityId\":null},{\"machineId\":\"YST18000334\",\"netCommunityId\":null},{\"machineId\":\"YST18000408\",\"netCommunityId\":null},{\"machineId\":\"YST18000409\",\"netCommunityId\":null},{\"machineId\":\"YST18000412\",\"netCommunityId\":null},{\"machineId\":\"YST18000522\",\"netCommunityId\":null},{\"machineId\":\"YST18000557\",\"netCommunityId\":null},{\"machineId\":\"YST18000558\",\"netCommunityId\":null},{\"machineId\":\"YST18000565\",\"netCommunityId\":null},{\"machineId\":\"YST18000567\",\"netCommunityId\":null},{\"machineId\":\"YST18000570\",\"netCommunityId\":null},{\"machineId\":\"YST18000575\",\"netCommunityId\":null},{\"machineId\":\"YST18000582\",\"netCommunityId\":null},{\"machineId\":\"YST18000583\",\"netCommunityId\":null},{\"machineId\":\"YST18000586\",\"netCommunityId\":null},{\"machineId\":\"YST18000589\",\"netCommunityId\":null},{\"machineId\":\"YST18000630\",\"netCommunityId\":null},{\"machineId\":\"YST18000668\",\"netCommunityId\":null},{\"machineId\":\"YST18000669\",\"netCommunityId\":null},{\"machineId\":\"YST18000673\",\"netCommunityId\":null},{\"machineId\":\"YST18000675\",\"netCommunityId\":null},{\"machineId\":\"YST18000679\",\"netCommunityId\":null},{\"machineId\":\"YST18000684\",\"netCommunityId\":null},{\"machineId\":\"YST18000687\",\"netCommunityId\":null},{\"machineId\":\"YST18000690\",\"netCommunityId\":null},{\"machineId\":\"YST18000712\",\"netCommunityId\":null},{\"machineId\":\"YST18001020\",\"netCommunityId\":null},{\"machineId\":\"YST18001021\",\"netCommunityId\":null},{\"machineId\":\"YST18001022\",\"netCommunityId\":null},{\"machineId\":\"YST18001031\",\"netCommunityId\":null},{\"machineId\":\"YST18001035\",\"netCommunityId\":null},{\"machineId\":\"YST18001087\",\"netCommunityId\":null},{\"machineId\":\"YST18001088\",\"netCommunityId\":null},{\"machineId\":\"YST18001098\",\"netCommunityId\":null},{\"machineId\":\"YST18001139\",\"netCommunityId\":null},{\"machineId\":\"YST18001146\",\"netCommunityId\":null},{\"machineId\":\"YST18001223\",\"netCommunityId\":null},{\"machineId\":\"YST18001227\",\"netCommunityId\":null},{\"machineId\":\"YST18001235\",\"netCommunityId\":null},{\"machineId\":\"YST18001241\",\"netCommunityId\":null},{\"machineId\":\"YST18001244\",\"netCommunityId\":null},{\"machineId\":\"YST18001369\",\"netCommunityId\":null},{\"machineId\":\"YST18001371\",\"netCommunityId\":null},{\"machineId\":\"YST18001378\",\"netCommunityId\":null},{\"machineId\":\"YST18001738\",\"netCommunityId\":null},{\"machineId\":\"YST18001866\",\"netCommunityId\":null},{\"machineId\":\"YST18001931\",\"netCommunityId\":null},{\"machineId\":\"YST18001939\",\"netCommunityId\":null},{\"machineId\":\"YST18001966\",\"netCommunityId\":null},{\"machineId\":\"YST18002012\",\"netCommunityId\":null},{\"machineId\":\"YST18002814\",\"netCommunityId\":null},{\"machineId\":\"YST18002824\",\"netCommunityId\":null},{\"machineId\":\"YST18003056\",\"netCommunityId\":null},{\"machineId\":\"YST18003869\",\"netCommunityId\":null},{\"machineId\":\"YST18004120\",\"netCommunityId\":null},{\"machineId\":\"YST18004332\",\"netCommunityId\":null},{\"machineId\":\"YST18004358\",\"netCommunityId\":null},{\"machineId\":\"YST18004424\",\"netCommunityId\":null},{\"machineId\":\"YST18004425\",\"netCommunityId\":null},{\"machineId\":\"YST18004686\",\"netCommunityId\":null},{\"machineId\":\"YST18004804\",\"netCommunityId\":null},{\"machineId\":\"YST18004834\",\"netCommunityId\":null},{\"machineId\":\"YST18004845\",\"netCommunityId\":null},{\"machineId\":\"YST18004874\",\"netCommunityId\":null},{\"machineId\":\"YST18004925\",\"netCommunityId\":null},{\"machineId\":\"YST18005035\",\"netCommunityId\":null},{\"machineId\":\"YST18005067\",\"netCommunityId\":null},{\"machineId\":\"YST18005115\",\"netCommunityId\":null},{\"machineId\":\"YST18005182\",\"netCommunityId\":null},{\"machineId\":\"YST18005191\",\"netCommunityId\":null},{\"machineId\":\"YST18005202\",\"netCommunityId\":null},{\"machineId\":\"YST18005252\",\"netCommunityId\":null},{\"machineId\":\"YST18005378\",\"netCommunityId\":null},{\"machineId\":\"YST18005389\",\"netCommunityId\":null},{\"machineId\":\"YST18005517\",\"netCommunityId\":null},{\"machineId\":\"YST18005679\",\"netCommunityId\":null},{\"machineId\":\"YST18005696\",\"netCommunityId\":null},{\"machineId\":\"YST18006016\",\"netCommunityId\":null},{\"machineId\":\"YST18006021\",\"netCommunityId\":null},{\"machineId\":\"YST18006071\",\"netCommunityId\":null},{\"machineId\":\"YST18006073\",\"netCommunityId\":null},{\"machineId\":\"YST18006092\",\"netCommunityId\":null},{\"machineId\":\"YST18006291\",\"netCommunityId\":null},{\"machineId\":\"YST18006357\",\"netCommunityId\":null},{\"machineId\":\"YST18006371\",\"netCommunityId\":null},{\"machineId\":\"YST18006414\",\"netCommunityId\":null},{\"machineId\":\"YST18006674\",\"netCommunityId\":null},{\"machineId\":\"YST18006843\",\"netCommunityId\":null},{\"machineId\":\"YST18006844\",\"netCommunityId\":null},{\"machineId\":\"YST18006845\",\"netCommunityId\":null},{\"machineId\":\"YST18006879\",\"netCommunityId\":null},{\"machineId\":\"YST18006892\",\"netCommunityId\":null},{\"machineId\":\"YST18006907\",\"netCommunityId\":null},{\"machineId\":\"YST18006927\",\"netCommunityId\":null},{\"machineId\":\"YST18007095\",\"netCommunityId\":null},{\"machineId\":\"YST18007105\",\"netCommunityId\":null},{\"machineId\":\"YST18007191\",\"netCommunityId\":null},{\"machineId\":\"YST18007192\",\"netCommunityId\":null},{\"machineId\":\"YST18007259\",\"netCommunityId\":null},{\"machineId\":\"YST18007262\",\"netCommunityId\":null},{\"machineId\":\"YST18007349\",\"netCommunityId\":null},{\"machineId\":\"YST18007540\",\"netCommunityId\":null},{\"machineId\":\"YST18007695\",\"netCommunityId\":null},{\"machineId\":\"YST18007786\",\"netCommunityId\":null},{\"machineId\":\"YST18007787\",\"netCommunityId\":null},{\"machineId\":\"YST18007788\",\"netCommunityId\":null},{\"machineId\":\"YST18007808\",\"netCommunityId\":null},{\"machineId\":\"YST18007841\",\"netCommunityId\":null},{\"machineId\":\"YST18007905\",\"netCommunityId\":null},{\"machineId\":\"YST18007908\",\"netCommunityId\":null},{\"machineId\":\"YST18007935\",\"netCommunityId\":null},{\"machineId\":\"YST18008122\",\"netCommunityId\":null},{\"machineId\":\"YST18008130\",\"netCommunityId\":null},{\"machineId\":\"YST18008164\",\"netCommunityId\":null},{\"machineId\":\"YST18008167\",\"netCommunityId\":null},{\"machineId\":\"YST18008178\",\"netCommunityId\":null},{\"machineId\":\"YST18008227\",\"netCommunityId\":null},{\"machineId\":\"YST18008245\",\"netCommunityId\":null},{\"machineId\":\"YST18008264\",\"netCommunityId\":null},{\"machineId\":\"YST18008268\",\"netCommunityId\":null},{\"machineId\":\"YST18008269\",\"netCommunityId\":null},{\"machineId\":\"YST18008285\",\"netCommunityId\":null},{\"machineId\":\"YST18008292\",\"netCommunityId\":null},{\"machineId\":\"YST18008296\",\"netCommunityId\":null},{\"machineId\":\"YST18008320\",\"netCommunityId\":null},{\"machineId\":\"YST18008321\",\"netCommunityId\":null},{\"machineId\":\"YST18008412\",\"netCommunityId\":null},{\"machineId\":\"YST18008457\",\"netCommunityId\":null},{\"machineId\":\"YST18008495\",\"netCommunityId\":null},{\"machineId\":\"YST18008496\",\"netCommunityId\":null},{\"machineId\":\"YST18008497\",\"netCommunityId\":null},{\"machineId\":\"YST18008498\",\"netCommunityId\":null},{\"machineId\":\"YST18008501\",\"netCommunityId\":null},{\"machineId\":\"YST18008510\",\"netCommunityId\":null},{\"machineId\":\"YST18008515\",\"netCommunityId\":null},{\"machineId\":\"YST18008516\",\"netCommunityId\":null},{\"machineId\":\"YST18008520\",\"netCommunityId\":null},{\"machineId\":\"YST18008523\",\"netCommunityId\":null},{\"machineId\":\"YST18008550\",\"netCommunityId\":null},{\"machineId\":\"YST18008553\",\"netCommunityId\":null},{\"machineId\":\"YST18008556\",\"netCommunityId\":null},{\"machineId\":\"YST18008708\",\"netCommunityId\":null},{\"machineId\":\"YST18008797\",\"netCommunityId\":null},{\"machineId\":\"YST18008802\",\"netCommunityId\":null},{\"machineId\":\"YST18008803\",\"netCommunityId\":null},{\"machineId\":\"YST18008843\",\"netCommunityId\":null},{\"machineId\":\"YST18008853\",\"netCommunityId\":null},{\"machineId\":\"YST18009007\",\"netCommunityId\":null},{\"machineId\":\"YST18009010\",\"netCommunityId\":null},{\"machineId\":\"YST18009454\",\"netCommunityId\":null},{\"machineId\":\"YST18009470\",\"netCommunityId\":null},{\"machineId\":\"YST18009488\",\"netCommunityId\":null},{\"machineId\":\"YST18009490\",\"netCommunityId\":null},{\"machineId\":\"YST18009492\",\"netCommunityId\":null},{\"machineId\":\"YST18010092\",\"netCommunityId\":null},{\"machineId\":\"YST18010098\",\"netCommunityId\":null},{\"machineId\":\"YST18010251\",\"netCommunityId\":null},{\"machineId\":\"YST18010253\",\"netCommunityId\":null},{\"machineId\":\"YST18010254\",\"netCommunityId\":null},{\"machineId\":\"YST18010256\",\"netCommunityId\":null},{\"machineId\":\"YST18010313\",\"netCommunityId\":null},{\"machineId\":\"YST18010321\",\"netCommunityId\":null},{\"machineId\":\"YST18010878\",\"netCommunityId\":null},{\"machineId\":\"YST18010879\",\"netCommunityId\":null},{\"machineId\":\"YST18010907\",\"netCommunityId\":null},{\"machineId\":\"YST18011172\",\"netCommunityId\":null},{\"machineId\":\"YST18011228\",\"netCommunityId\":null},{\"machineId\":\"YST18011315\",\"netCommunityId\":null},{\"machineId\":\"YST18011319\",\"netCommunityId\":null},{\"machineId\":\"YST18012673\",\"netCommunityId\":null},{\"machineId\":\"YST18012897\",\"netCommunityId\":null},{\"machineId\":\"YST18012939\",\"netCommunityId\":null},{\"machineId\":\"YST18013014\",\"netCommunityId\":null},{\"machineId\":\"YST18013145\",\"netCommunityId\":null},{\"machineId\":\"YST18015084\",\"netCommunityId\":null},{\"machineId\":\"YST18015088\",\"netCommunityId\":null},{\"machineId\":\"YST18015098\",\"netCommunityId\":null},{\"machineId\":\"YST18015100\",\"netCommunityId\":null},{\"machineId\":\"YST18015105\",\"netCommunityId\":null},{\"machineId\":\"YST18015108\",\"netCommunityId\":null},{\"machineId\":\"YST18015111\",\"netCommunityId\":null},{\"machineId\":\"YST18015117\",\"netCommunityId\":null},{\"machineId\":\"YST18015120\",\"netCommunityId\":null},{\"machineId\":\"YST18015129\",\"netCommunityId\":null},{\"machineId\":\"YST18015130\",\"netCommunityId\":null},{\"machineId\":\"YST18015138\",\"netCommunityId\":null},{\"machineId\":\"YST18015167\",\"netCommunityId\":null},{\"machineId\":\"YST18015187\",\"netCommunityId\":null},{\"machineId\":\"YST18015240\",\"netCommunityId\":null},{\"machineId\":\"YST18047810\",\"netCommunityId\":null},{\"machineId\":\"YST18047818\",\"netCommunityId\":null},{\"machineId\":\"YST18047867\",\"netCommunityId\":null},{\"machineId\":\"YST18047885\",\"netCommunityId\":null},{\"machineId\":\"YST18048165\",\"netCommunityId\":null},{\"machineId\":\"YST18049260\",\"netCommunityId\":null},{\"machineId\":\"YST18049262\",\"netCommunityId\":null},{\"machineId\":\"YST18049263\",\"netCommunityId\":null},{\"machineId\":\"YST18049286\",\"netCommunityId\":null},{\"machineId\":\"YST18049287\",\"netCommunityId\":null},{\"machineId\":\"YST18049288\",\"netCommunityId\":null},{\"machineId\":\"YST18049348\",\"netCommunityId\":null},{\"machineId\":\"YST18049359\",\"netCommunityId\":null},{\"machineId\":\"YST18049361\",\"netCommunityId\":null},{\"machineId\":\"YST18049362\",\"netCommunityId\":null},{\"machineId\":\"YST18049364\",\"netCommunityId\":null},{\"machineId\":\"YST18049369\",\"netCommunityId\":null},{\"machineId\":\"YST18049374\",\"netCommunityId\":null},{\"machineId\":\"YST18049382\",\"netCommunityId\":null},{\"machineId\":\"YST18049437\",\"netCommunityId\":null},{\"machineId\":\"YST18049438\",\"netCommunityId\":null},{\"machineId\":\"YST18049443\",\"netCommunityId\":null},{\"machineId\":\"YST18049444\",\"netCommunityId\":null},{\"machineId\":\"YST18049447\",\"netCommunityId\":null},{\"machineId\":\"YST18049448\",\"netCommunityId\":null},{\"machineId\":\"YST18049450\",\"netCommunityId\":null},{\"machineId\":\"YST18049454\",\"netCommunityId\":null},{\"machineId\":\"YST18049459\",\"netCommunityId\":null},{\"machineId\":\"YST18049460\",\"netCommunityId\":null},{\"machineId\":\"YST18049465\",\"netCommunityId\":null},{\"machineId\":\"YST18049482\",\"netCommunityId\":null},{\"machineId\":\"YST18049493\",\"netCommunityId\":null},{\"machineId\":\"YST18049516\",\"netCommunityId\":null},{\"machineId\":\"YST18049520\",\"netCommunityId\":null},{\"machineId\":\"YST18049526\",\"netCommunityId\":null},{\"machineId\":\"YST18050514\",\"netCommunityId\":null},{\"machineId\":\"YST18050538\",\"netCommunityId\":null},{\"machineId\":\"YST18050539\",\"netCommunityId\":null},{\"machineId\":\"YST18050540\",\"netCommunityId\":null},{\"machineId\":\"YST18050541\",\"netCommunityId\":null},{\"machineId\":\"YST18050562\",\"netCommunityId\":null},{\"machineId\":\"YST18050570\",\"netCommunityId\":null},{\"machineId\":\"YST18050585\",\"netCommunityId\":null},{\"machineId\":\"YST18050589\",\"netCommunityId\":null},{\"machineId\":\"YST18050597\",\"netCommunityId\":null},{\"machineId\":\"YST18050600\",\"netCommunityId\":null},{\"machineId\":\"YST18050601\",\"netCommunityId\":null},{\"machineId\":\"YST18050608\",\"netCommunityId\":null},{\"machineId\":\"YST18050611\",\"netCommunityId\":null},{\"machineId\":\"YST18050618\",\"netCommunityId\":null},{\"machineId\":\"YST18050619\",\"netCommunityId\":null},{\"machineId\":\"YST18050620\",\"netCommunityId\":null},{\"machineId\":\"YST18050623\",\"netCommunityId\":null},{\"machineId\":\"YST18050624\",\"netCommunityId\":null},{\"machineId\":\"YST18050625\",\"netCommunityId\":null},{\"machineId\":\"YST18050626\",\"netCommunityId\":null},{\"machineId\":\"YST18050627\",\"netCommunityId\":null},{\"machineId\":\"YST18050628\",\"netCommunityId\":null},{\"machineId\":\"YST18050630\",\"netCommunityId\":null},{\"machineId\":\"YST18050631\",\"netCommunityId\":null},{\"machineId\":\"YST18050638\",\"netCommunityId\":null},{\"machineId\":\"YST18050639\",\"netCommunityId\":null},{\"machineId\":\"YST18050641\",\"netCommunityId\":null},{\"machineId\":\"YST18050642\",\"netCommunityId\":null},{\"machineId\":\"YST18050647\",\"netCommunityId\":null},{\"machineId\":\"YST18050653\",\"netCommunityId\":null},{\"machineId\":\"YST18050656\",\"netCommunityId\":null},{\"machineId\":\"YST18050659\",\"netCommunityId\":null},{\"machineId\":\"YST18050669\",\"netCommunityId\":null},{\"machineId\":\"YST18050670\",\"netCommunityId\":null},{\"machineId\":\"YST18050671\",\"netCommunityId\":null},{\"machineId\":\"YST18050672\",\"netCommunityId\":null},{\"machineId\":\"YST18050676\",\"netCommunityId\":null},{\"machineId\":\"YST18050677\",\"netCommunityId\":null},{\"machineId\":\"YST18050678\",\"netCommunityId\":null},{\"machineId\":\"YST18050682\",\"netCommunityId\":null},{\"machineId\":\"YST18050685\",\"netCommunityId\":null},{\"machineId\":\"YST18050686\",\"netCommunityId\":null},{\"machineId\":\"YST18050687\",\"netCommunityId\":null},{\"machineId\":\"YST18053008\",\"netCommunityId\":null},{\"machineId\":\"YST18053014\",\"netCommunityId\":null},{\"machineId\":\"YST18053022\",\"netCommunityId\":null},{\"machineId\":\"YST18053030\",\"netCommunityId\":null},{\"machineId\":\"YST18053039\",\"netCommunityId\":null},{\"machineId\":\"YST18053043\",\"netCommunityId\":null},{\"machineId\":\"YST18053095\",\"netCommunityId\":null},{\"machineId\":\"YST18053098\",\"netCommunityId\":null},{\"machineId\":\"YST18053100\",\"netCommunityId\":null},{\"machineId\":\"YST18053105\",\"netCommunityId\":null},{\"machineId\":\"YST18053107\",\"netCommunityId\":null},{\"machineId\":\"YST18053108\",\"netCommunityId\":null},{\"machineId\":\"YST18053118\",\"netCommunityId\":null},{\"machineId\":\"YST18053120\",\"netCommunityId\":null},{\"machineId\":\"YST18053124\",\"netCommunityId\":null},{\"machineId\":\"YST18053130\",\"netCommunityId\":null},{\"machineId\":\"YST18053131\",\"netCommunityId\":null},{\"machineId\":\"YST18053147\",\"netCommunityId\":null},{\"machineId\":\"YST18053157\",\"netCommunityId\":null},{\"machineId\":\"YST18053158\",\"netCommunityId\":null},{\"machineId\":\"YST18053164\",\"netCommunityId\":null},{\"machineId\":\"YST18053171\",\"netCommunityId\":null},{\"machineId\":\"YST18053173\",\"netCommunityId\":null},{\"machineId\":\"YST18053178\",\"netCommunityId\":null},{\"machineId\":\"YST18053186\",\"netCommunityId\":null},{\"machineId\":\"YST18053189\",\"netCommunityId\":null},{\"machineId\":\"YST18053210\",\"netCommunityId\":null},{\"machineId\":\"YST18053211\",\"netCommunityId\":null},{\"machineId\":\"YST18053215\",\"netCommunityId\":null},{\"machineId\":\"YST18053220\",\"netCommunityId\":null},{\"machineId\":\"YST18053223\",\"netCommunityId\":null},{\"machineId\":\"YST18053225\",\"netCommunityId\":null},{\"machineId\":\"YST18053227\",\"netCommunityId\":null}],\"from\":null,\"ruleId\":10768,\"orgId\":null,\"token\":null,\"timestamp\":null},\"city\":310000,\"statusValue\":null,\"tagIds\":null,\"issueSystemType\":\"zfj\",\"description\":\"3-4户限活动\",\"alterCouponSource\":0,\"primaryCouponSource\":0,\"purchaseType\":null,\"issueStatus\":null,\"issueStartTime\":\"2019-03-14 14:35:50\",\"ruleType\":\"F\",\"isNewsNeeded\":0,\"familyLimit\":1,\"fileUrl\":\"http://10.213.3.46:8080/NFSpring_file_service/handleFile/vendorFile/picture/1413851cc5cf8c9545faafe1037e5bb6.png\",\"machineType\":null,\"createDate\":\"2019-03-04 18:58:16\",\"memberLevel\":[-3],\"couponTemplateId\":null,\"isTempMsgNeeded\":1,\"canton\":null,\"alterRuleId\":null}],\"couponTemplateId\":null,\"token\":null,\"t\":null,\"name\":null,\"sendType\":null,\"canton\":null,\"currentPage\":1,\"category\":1,\"startDate\":null,\"status\":2},\"success\":true}";
        String expect6 = "{ \"msg\":null, \"code\":null, \"data\":{ \"totalElements\":0, \"currentPage\":1, \"totalPage\":0, \"pageSize\":10, \"token\":null, \"timestamp\":null, \"modifyId\":\"124153\", \"orgId\":8334, \"orgIdList\":null, \"from\":null, \"t\":null, \"id\":null, \"name\":null, \"couponTemplateId\":null, \"ruleType\":null, \"createDate\":null, \"startDate\":null, \"endDate\":null, \"status\":2, \"systemType\":null, \"sendType\":null, \"province\":null, \"city\":null, \"canton\":null, \"issueStatus\":null, \"couponRules\":[ ], \"category\":1, \"couponSource\":null }, \"success\":true }";
        String response6 = "{\"msg\":null,\"code\":null,\"data\":{\"couponSource\":null,\"endDate\":null,\"city\":null,\"pageSize\":10,\"orgId\":8334,\"totalElements\":1,\"issueStatus\":null,\"province\":null,\"orgIdList\":null,\"ruleType\":null,\"systemType\":null,\"from\":null,\"id\":null,\"timestamp\":null,\"createDate\":null,\"modifyId\":\"124153\",\"totalPage\":1,\"couponRules\":[{\"prodList\":[],\"issueEndTime\":\"2019-03-14 23:59:59\",\"couponTemplateIds\":[{\"templateType\":\"D\",\"endDate\":null,\"city\":null,\"modifyStatus\":null,\"discount\":0.50,\"description\":null,\"title\":\"量贩机折扣券\",\"modifyUser\":null,\"province\":null,\"systemType\":null,\"id\":2046,\"vaildTimeType\":null,\"subheading\":null,\"askAmount\":null,\"createDate\":null,\"perIssue\":1,\"templateNo\":null,\"amount\":null,\"modifyDate\":null,\"templateTypeName\":\"折扣券\",\"canton\":null,\"createUser\":null,\"vaildTime\":null,\"startDate\":null,\"status\":null}],\"firstText\":\"3-4户限活动\",\"province\":310000,\"newsTitle\":null,\"newsDesc\":null,\"newsPicUrl\":null,\"id\":10768,\"bindCommunity\":0,\"maxIssueCount\":100,\"marketingType\":1,\"modifyDate\":\"2019-03-13 15:04:12\",\"modifyId\":null,\"remarkText\":\"点击入驻领券\",\"newsPicId\":null,\"memberLimit\":1,\"activityUrl\":null,\"createId\":null,\"sendType\":1,\"name\":\"3-4户限活动\",\"purposeList\":[1,2],\"perIssueCount\":null,\"status\":2,\"alterCouponList\":[{\"templateType\":\"D\",\"endDate\":null,\"city\":null,\"modifyStatus\":null,\"discount\":0.50,\"description\":null,\"title\":\"自贩机折扣券\",\"modifyUser\":null,\"province\":null,\"systemType\":null,\"id\":2039,\"vaildTimeType\":null,\"subheading\":null,\"askAmount\":null,\"createDate\":null,\"perIssue\":1,\"templateNo\":null,\"amount\":null,\"modifyDate\":null,\"templateTypeName\":\"折扣券\",\"canton\":null,\"createUser\":null,\"vaildTime\":null,\"startDate\":null,\"status\":null}],\"fileId\":40840,\"cityRange\":[],\"hasAlterCoupon\":1,\"ruleMachineOfficeBean\":{\"officeIds\":[273,362,363,581,898,1443,1445,1446,1447,1449,1494,2926,2927,4761,4768,6224,6338,7885,7970,9155,9156,9157,9226,51180,51182,51183,51185],\"t\":null,\"modifyId\":null,\"orgIdList\":null,\"machineIds\":[{\"machineId\":\"YST18000019\",\"netCommunityId\":null},{\"machineId\":\"YST18000066\",\"netCommunityId\":null},{\"machineId\":\"YST18000143\",\"netCommunityId\":null},{\"machineId\":\"YST18000173\",\"netCommunityId\":null},{\"machineId\":\"YST18000306\",\"netCommunityId\":null},{\"machineId\":\"YST18000326\",\"netCommunityId\":null},{\"machineId\":\"YST18000329\",\"netCommunityId\":null},{\"machineId\":\"YST18000330\",\"netCommunityId\":null},{\"machineId\":\"YST18000331\",\"netCommunityId\":null},{\"machineId\":\"YST18000334\",\"netCommunityId\":null},{\"machineId\":\"YST18000408\",\"netCommunityId\":null},{\"machineId\":\"YST18000409\",\"netCommunityId\":null},{\"machineId\":\"YST18000412\",\"netCommunityId\":null},{\"machineId\":\"YST18000522\",\"netCommunityId\":null},{\"machineId\":\"YST18000557\",\"netCommunityId\":null},{\"machineId\":\"YST18000558\",\"netCommunityId\":null},{\"machineId\":\"YST18000565\",\"netCommunityId\":null},{\"machineId\":\"YST18000567\",\"netCommunityId\":null},{\"machineId\":\"YST18000570\",\"netCommunityId\":null},{\"machineId\":\"YST18000575\",\"netCommunityId\":null},{\"machineId\":\"YST18000582\",\"netCommunityId\":null},{\"machineId\":\"YST18000583\",\"netCommunityId\":null},{\"machineId\":\"YST18000586\",\"netCommunityId\":null},{\"machineId\":\"YST18000589\",\"netCommunityId\":null},{\"machineId\":\"YST18000630\",\"netCommunityId\":null},{\"machineId\":\"YST18000668\",\"netCommunityId\":null},{\"machineId\":\"YST18000669\",\"netCommunityId\":null},{\"machineId\":\"YST18000673\",\"netCommunityId\":null},{\"machineId\":\"YST18000675\",\"netCommunityId\":null},{\"machineId\":\"YST18000679\",\"netCommunityId\":null},{\"machineId\":\"YST18000684\",\"netCommunityId\":null},{\"machineId\":\"YST18000687\",\"netCommunityId\":null},{\"machineId\":\"YST18000690\",\"netCommunityId\":null},{\"machineId\":\"YST18000712\",\"netCommunityId\":null},{\"machineId\":\"YST18001020\",\"netCommunityId\":null},{\"machineId\":\"YST18001021\",\"netCommunityId\":null},{\"machineId\":\"YST18001022\",\"netCommunityId\":null},{\"machineId\":\"YST18001031\",\"netCommunityId\":null},{\"machineId\":\"YST18001035\",\"netCommunityId\":null},{\"machineId\":\"YST18001087\",\"netCommunityId\":null},{\"machineId\":\"YST18001088\",\"netCommunityId\":null},{\"machineId\":\"YST18001098\",\"netCommunityId\":null},{\"machineId\":\"YST18001139\",\"netCommunityId\":null},{\"machineId\":\"YST18001146\",\"netCommunityId\":null},{\"machineId\":\"YST18001223\",\"netCommunityId\":null},{\"machineId\":\"YST18001227\",\"netCommunityId\":null},{\"machineId\":\"YST18001235\",\"netCommunityId\":null},{\"machineId\":\"YST18001241\",\"netCommunityId\":null},{\"machineId\":\"YST18001244\",\"netCommunityId\":null},{\"machineId\":\"YST18001369\",\"netCommunityId\":null},{\"machineId\":\"YST18001371\",\"netCommunityId\":null},{\"machineId\":\"YST18001378\",\"netCommunityId\":null},{\"machineId\":\"YST18001738\",\"netCommunityId\":null},{\"machineId\":\"YST18001866\",\"netCommunityId\":null},{\"machineId\":\"YST18001931\",\"netCommunityId\":null},{\"machineId\":\"YST18001939\",\"netCommunityId\":null},{\"machineId\":\"YST18001966\",\"netCommunityId\":null},{\"machineId\":\"YST18002012\",\"netCommunityId\":null},{\"machineId\":\"YST18002814\",\"netCommunityId\":null},{\"machineId\":\"YST18002824\",\"netCommunityId\":null},{\"machineId\":\"YST18003056\",\"netCommunityId\":null},{\"machineId\":\"YST18003869\",\"netCommunityId\":null},{\"machineId\":\"YST18004120\",\"netCommunityId\":null},{\"machineId\":\"YST18004332\",\"netCommunityId\":null},{\"machineId\":\"YST18004358\",\"netCommunityId\":null},{\"machineId\":\"YST18004424\",\"netCommunityId\":null},{\"machineId\":\"YST18004425\",\"netCommunityId\":null},{\"machineId\":\"YST18004686\",\"netCommunityId\":null},{\"machineId\":\"YST18004804\",\"netCommunityId\":null},{\"machineId\":\"YST18004834\",\"netCommunityId\":null},{\"machineId\":\"YST18004845\",\"netCommunityId\":null},{\"machineId\":\"YST18004874\",\"netCommunityId\":null},{\"machineId\":\"YST18004925\",\"netCommunityId\":null},{\"machineId\":\"YST18005035\",\"netCommunityId\":null},{\"machineId\":\"YST18005067\",\"netCommunityId\":null},{\"machineId\":\"YST18005115\",\"netCommunityId\":null},{\"machineId\":\"YST18005182\",\"netCommunityId\":null},{\"machineId\":\"YST18005191\",\"netCommunityId\":null},{\"machineId\":\"YST18005202\",\"netCommunityId\":null},{\"machineId\":\"YST18005252\",\"netCommunityId\":null},{\"machineId\":\"YST18005378\",\"netCommunityId\":null},{\"machineId\":\"YST18005389\",\"netCommunityId\":null},{\"machineId\":\"YST18005517\",\"netCommunityId\":null},{\"machineId\":\"YST18005679\",\"netCommunityId\":null},{\"machineId\":\"YST18005696\",\"netCommunityId\":null},{\"machineId\":\"YST18006016\",\"netCommunityId\":null},{\"machineId\":\"YST18006021\",\"netCommunityId\":null},{\"machineId\":\"YST18006071\",\"netCommunityId\":null},{\"machineId\":\"YST18006073\",\"netCommunityId\":null},{\"machineId\":\"YST18006092\",\"netCommunityId\":null},{\"machineId\":\"YST18006291\",\"netCommunityId\":null},{\"machineId\":\"YST18006357\",\"netCommunityId\":null},{\"machineId\":\"YST18006371\",\"netCommunityId\":null},{\"machineId\":\"YST18006414\",\"netCommunityId\":null},{\"machineId\":\"YST18006674\",\"netCommunityId\":null},{\"machineId\":\"YST18006843\",\"netCommunityId\":null},{\"machineId\":\"YST18006844\",\"netCommunityId\":null},{\"machineId\":\"YST18006845\",\"netCommunityId\":null},{\"machineId\":\"YST18006879\",\"netCommunityId\":null},{\"machineId\":\"YST18006892\",\"netCommunityId\":null},{\"machineId\":\"YST18006907\",\"netCommunityId\":null},{\"machineId\":\"YST18006927\",\"netCommunityId\":null},{\"machineId\":\"YST18007095\",\"netCommunityId\":null},{\"machineId\":\"YST18007105\",\"netCommunityId\":null},{\"machineId\":\"YST18007191\",\"netCommunityId\":null},{\"machineId\":\"YST18007192\",\"netCommunityId\":null},{\"machineId\":\"YST18007259\",\"netCommunityId\":null},{\"machineId\":\"YST18007262\",\"netCommunityId\":null},{\"machineId\":\"YST18007349\",\"netCommunityId\":null},{\"machineId\":\"YST18007540\",\"netCommunityId\":null},{\"machineId\":\"YST18007695\",\"netCommunityId\":null},{\"machineId\":\"YST18007786\",\"netCommunityId\":null},{\"machineId\":\"YST18007787\",\"netCommunityId\":null},{\"machineId\":\"YST18007788\",\"netCommunityId\":null},{\"machineId\":\"YST18007808\",\"netCommunityId\":null},{\"machineId\":\"YST18007841\",\"netCommunityId\":null},{\"machineId\":\"YST18007905\",\"netCommunityId\":null},{\"machineId\":\"YST18007908\",\"netCommunityId\":null},{\"machineId\":\"YST18007935\",\"netCommunityId\":null},{\"machineId\":\"YST18008122\",\"netCommunityId\":null},{\"machineId\":\"YST18008130\",\"netCommunityId\":null},{\"machineId\":\"YST18008164\",\"netCommunityId\":null},{\"machineId\":\"YST18008167\",\"netCommunityId\":null},{\"machineId\":\"YST18008178\",\"netCommunityId\":null},{\"machineId\":\"YST18008227\",\"netCommunityId\":null},{\"machineId\":\"YST18008245\",\"netCommunityId\":null},{\"machineId\":\"YST18008264\",\"netCommunityId\":null},{\"machineId\":\"YST18008268\",\"netCommunityId\":null},{\"machineId\":\"YST18008269\",\"netCommunityId\":null},{\"machineId\":\"YST18008285\",\"netCommunityId\":null},{\"machineId\":\"YST18008292\",\"netCommunityId\":null},{\"machineId\":\"YST18008296\",\"netCommunityId\":null},{\"machineId\":\"YST18008320\",\"netCommunityId\":null},{\"machineId\":\"YST18008321\",\"netCommunityId\":null},{\"machineId\":\"YST18008412\",\"netCommunityId\":null},{\"machineId\":\"YST18008457\",\"netCommunityId\":null},{\"machineId\":\"YST18008495\",\"netCommunityId\":null},{\"machineId\":\"YST18008496\",\"netCommunityId\":null},{\"machineId\":\"YST18008497\",\"netCommunityId\":null},{\"machineId\":\"YST18008498\",\"netCommunityId\":null},{\"machineId\":\"YST18008501\",\"netCommunityId\":null},{\"machineId\":\"YST18008510\",\"netCommunityId\":null},{\"machineId\":\"YST18008515\",\"netCommunityId\":null},{\"machineId\":\"YST18008516\",\"netCommunityId\":null},{\"machineId\":\"YST18008520\",\"netCommunityId\":null},{\"machineId\":\"YST18008523\",\"netCommunityId\":null},{\"machineId\":\"YST18008550\",\"netCommunityId\":null},{\"machineId\":\"YST18008553\",\"netCommunityId\":null},{\"machineId\":\"YST18008556\",\"netCommunityId\":null},{\"machineId\":\"YST18008708\",\"netCommunityId\":null},{\"machineId\":\"YST18008797\",\"netCommunityId\":null},{\"machineId\":\"YST18008802\",\"netCommunityId\":null},{\"machineId\":\"YST18008803\",\"netCommunityId\":null},{\"machineId\":\"YST18008843\",\"netCommunityId\":null},{\"machineId\":\"YST18008853\",\"netCommunityId\":null},{\"machineId\":\"YST18009007\",\"netCommunityId\":null},{\"machineId\":\"YST18009010\",\"netCommunityId\":null},{\"machineId\":\"YST18009454\",\"netCommunityId\":null},{\"machineId\":\"YST18009470\",\"netCommunityId\":null},{\"machineId\":\"YST18009488\",\"netCommunityId\":null},{\"machineId\":\"YST18009490\",\"netCommunityId\":null},{\"machineId\":\"YST18009492\",\"netCommunityId\":null},{\"machineId\":\"YST18010092\",\"netCommunityId\":null},{\"machineId\":\"YST18010098\",\"netCommunityId\":null},{\"machineId\":\"YST18010251\",\"netCommunityId\":null},{\"machineId\":\"YST18010253\",\"netCommunityId\":null},{\"machineId\":\"YST18010254\",\"netCommunityId\":null},{\"machineId\":\"YST18010256\",\"netCommunityId\":null},{\"machineId\":\"YST18010313\",\"netCommunityId\":null},{\"machineId\":\"YST18010321\",\"netCommunityId\":null},{\"machineId\":\"YST18010878\",\"netCommunityId\":null},{\"machineId\":\"YST18010879\",\"netCommunityId\":null},{\"machineId\":\"YST18010907\",\"netCommunityId\":null},{\"machineId\":\"YST18011172\",\"netCommunityId\":null},{\"machineId\":\"YST18011228\",\"netCommunityId\":null},{\"machineId\":\"YST18011315\",\"netCommunityId\":null},{\"machineId\":\"YST18011319\",\"netCommunityId\":null},{\"machineId\":\"YST18012673\",\"netCommunityId\":null},{\"machineId\":\"YST18012897\",\"netCommunityId\":null},{\"machineId\":\"YST18012939\",\"netCommunityId\":null},{\"machineId\":\"YST18013014\",\"netCommunityId\":null},{\"machineId\":\"YST18013145\",\"netCommunityId\":null},{\"machineId\":\"YST18015084\",\"netCommunityId\":null},{\"machineId\":\"YST18015088\",\"netCommunityId\":null},{\"machineId\":\"YST18015098\",\"netCommunityId\":null},{\"machineId\":\"YST18015100\",\"netCommunityId\":null},{\"machineId\":\"YST18015105\",\"netCommunityId\":null},{\"machineId\":\"YST18015108\",\"netCommunityId\":null},{\"machineId\":\"YST18015111\",\"netCommunityId\":null},{\"machineId\":\"YST18015117\",\"netCommunityId\":null},{\"machineId\":\"YST18015120\",\"netCommunityId\":null},{\"machineId\":\"YST18015129\",\"netCommunityId\":null},{\"machineId\":\"YST18015130\",\"netCommunityId\":null},{\"machineId\":\"YST18015138\",\"netCommunityId\":null},{\"machineId\":\"YST18015167\",\"netCommunityId\":null},{\"machineId\":\"YST18015187\",\"netCommunityId\":null},{\"machineId\":\"YST18015240\",\"netCommunityId\":null},{\"machineId\":\"YST18047810\",\"netCommunityId\":null},{\"machineId\":\"YST18047818\",\"netCommunityId\":null},{\"machineId\":\"YST18047867\",\"netCommunityId\":null},{\"machineId\":\"YST18047885\",\"netCommunityId\":null},{\"machineId\":\"YST18048165\",\"netCommunityId\":null},{\"machineId\":\"YST18049260\",\"netCommunityId\":null},{\"machineId\":\"YST18049262\",\"netCommunityId\":null},{\"machineId\":\"YST18049263\",\"netCommunityId\":null},{\"machineId\":\"YST18049286\",\"netCommunityId\":null},{\"machineId\":\"YST18049287\",\"netCommunityId\":null},{\"machineId\":\"YST18049288\",\"netCommunityId\":null},{\"machineId\":\"YST18049348\",\"netCommunityId\":null},{\"machineId\":\"YST18049359\",\"netCommunityId\":null},{\"machineId\":\"YST18049361\",\"netCommunityId\":null},{\"machineId\":\"YST18049362\",\"netCommunityId\":null},{\"machineId\":\"YST18049364\",\"netCommunityId\":null},{\"machineId\":\"YST18049369\",\"netCommunityId\":null},{\"machineId\":\"YST18049374\",\"netCommunityId\":null},{\"machineId\":\"YST18049382\",\"netCommunityId\":null},{\"machineId\":\"YST18049437\",\"netCommunityId\":null},{\"machineId\":\"YST18049438\",\"netCommunityId\":null},{\"machineId\":\"YST18049443\",\"netCommunityId\":null},{\"machineId\":\"YST18049444\",\"netCommunityId\":null},{\"machineId\":\"YST18049447\",\"netCommunityId\":null},{\"machineId\":\"YST18049448\",\"netCommunityId\":null},{\"machineId\":\"YST18049450\",\"netCommunityId\":null},{\"machineId\":\"YST18049454\",\"netCommunityId\":null},{\"machineId\":\"YST18049459\",\"netCommunityId\":null},{\"machineId\":\"YST18049460\",\"netCommunityId\":null},{\"machineId\":\"YST18049465\",\"netCommunityId\":null},{\"machineId\":\"YST18049482\",\"netCommunityId\":null},{\"machineId\":\"YST18049493\",\"netCommunityId\":null},{\"machineId\":\"YST18049516\",\"netCommunityId\":null},{\"machineId\":\"YST18049520\",\"netCommunityId\":null},{\"machineId\":\"YST18049526\",\"netCommunityId\":null},{\"machineId\":\"YST18050514\",\"netCommunityId\":null},{\"machineId\":\"YST18050538\",\"netCommunityId\":null},{\"machineId\":\"YST18050539\",\"netCommunityId\":null},{\"machineId\":\"YST18050540\",\"netCommunityId\":null},{\"machineId\":\"YST18050541\",\"netCommunityId\":null},{\"machineId\":\"YST18050562\",\"netCommunityId\":null},{\"machineId\":\"YST18050570\",\"netCommunityId\":null},{\"machineId\":\"YST18050585\",\"netCommunityId\":null},{\"machineId\":\"YST18050589\",\"netCommunityId\":null},{\"machineId\":\"YST18050597\",\"netCommunityId\":null},{\"machineId\":\"YST18050600\",\"netCommunityId\":null},{\"machineId\":\"YST18050601\",\"netCommunityId\":null},{\"machineId\":\"YST18050608\",\"netCommunityId\":null},{\"machineId\":\"YST18050611\",\"netCommunityId\":null},{\"machineId\":\"YST18050618\",\"netCommunityId\":null},{\"machineId\":\"YST18050619\",\"netCommunityId\":null},{\"machineId\":\"YST18050620\",\"netCommunityId\":null},{\"machineId\":\"YST18050623\",\"netCommunityId\":null},{\"machineId\":\"YST18050624\",\"netCommunityId\":null},{\"machineId\":\"YST18050625\",\"netCommunityId\":null},{\"machineId\":\"YST18050626\",\"netCommunityId\":null},{\"machineId\":\"YST18050627\",\"netCommunityId\":null},{\"machineId\":\"YST18050628\",\"netCommunityId\":null},{\"machineId\":\"YST18050630\",\"netCommunityId\":null},{\"machineId\":\"YST18050631\",\"netCommunityId\":null},{\"machineId\":\"YST18050638\",\"netCommunityId\":null},{\"machineId\":\"YST18050639\",\"netCommunityId\":null},{\"machineId\":\"YST18050641\",\"netCommunityId\":null},{\"machineId\":\"YST18050642\",\"netCommunityId\":null},{\"machineId\":\"YST18050647\",\"netCommunityId\":null},{\"machineId\":\"YST18050653\",\"netCommunityId\":null},{\"machineId\":\"YST18050656\",\"netCommunityId\":null},{\"machineId\":\"YST18050659\",\"netCommunityId\":null},{\"machineId\":\"YST18050669\",\"netCommunityId\":null},{\"machineId\":\"YST18050670\",\"netCommunityId\":null},{\"machineId\":\"YST18050671\",\"netCommunityId\":null},{\"machineId\":\"YST18050672\",\"netCommunityId\":null},{\"machineId\":\"YST18050676\",\"netCommunityId\":null},{\"machineId\":\"YST18050677\",\"netCommunityId\":null},{\"machineId\":\"YST18050678\",\"netCommunityId\":null},{\"machineId\":\"YST18050682\",\"netCommunityId\":null},{\"machineId\":\"YST18050685\",\"netCommunityId\":null},{\"machineId\":\"YST18050686\",\"netCommunityId\":null},{\"machineId\":\"YST18050687\",\"netCommunityId\":null},{\"machineId\":\"YST18053008\",\"netCommunityId\":null},{\"machineId\":\"YST18053014\",\"netCommunityId\":null},{\"machineId\":\"YST18053022\",\"netCommunityId\":null},{\"machineId\":\"YST18053030\",\"netCommunityId\":null},{\"machineId\":\"YST18053039\",\"netCommunityId\":null},{\"machineId\":\"YST18053043\",\"netCommunityId\":null},{\"machineId\":\"YST18053095\",\"netCommunityId\":null},{\"machineId\":\"YST18053098\",\"netCommunityId\":null},{\"machineId\":\"YST18053100\",\"netCommunityId\":null},{\"machineId\":\"YST18053105\",\"netCommunityId\":null},{\"machineId\":\"YST18053107\",\"netCommunityId\":null},{\"machineId\":\"YST18053108\",\"netCommunityId\":null},{\"machineId\":\"YST18053118\",\"netCommunityId\":null},{\"machineId\":\"YST18053120\",\"netCommunityId\":null},{\"machineId\":\"YST18053124\",\"netCommunityId\":null},{\"machineId\":\"YST18053130\",\"netCommunityId\":null},{\"machineId\":\"YST18053131\",\"netCommunityId\":null},{\"machineId\":\"YST18053147\",\"netCommunityId\":null},{\"machineId\":\"YST18053157\",\"netCommunityId\":null},{\"machineId\":\"YST18053158\",\"netCommunityId\":null},{\"machineId\":\"YST18053164\",\"netCommunityId\":null},{\"machineId\":\"YST18053171\",\"netCommunityId\":null},{\"machineId\":\"YST18053173\",\"netCommunityId\":null},{\"machineId\":\"YST18053178\",\"netCommunityId\":null},{\"machineId\":\"YST18053186\",\"netCommunityId\":null},{\"machineId\":\"YST18053189\",\"netCommunityId\":null},{\"machineId\":\"YST18053210\",\"netCommunityId\":null},{\"machineId\":\"YST18053211\",\"netCommunityId\":null},{\"machineId\":\"YST18053215\",\"netCommunityId\":null},{\"machineId\":\"YST18053220\",\"netCommunityId\":null},{\"machineId\":\"YST18053223\",\"netCommunityId\":null},{\"machineId\":\"YST18053225\",\"netCommunityId\":null},{\"machineId\":\"YST18053227\",\"netCommunityId\":null}],\"from\":null,\"ruleId\":10768,\"orgId\":null,\"token\":null,\"timestamp\":null},\"city\":310000,\"statusValue\":null,\"tagIds\":null,\"issueSystemType\":\"zfj\",\"description\":\"3-4户限活动\",\"alterCouponSource\":0,\"primaryCouponSource\":0,\"purchaseType\":null,\"issueStatus\":null,\"issueStartTime\":\"2019-03-14 14:35:50\",\"ruleType\":\"F\",\"isNewsNeeded\":0,\"familyLimit\":1,\"fileUrl\":\"http://10.213.3.46:8080/NFSpring_file_service/handleFile/vendorFile/picture/1413851cc5cf8c9545faafe1037e5bb6.png\",\"machineType\":null,\"createDate\":\"2019-03-04 18:58:16\",\"memberLevel\":[-3],\"couponTemplateId\":null,\"isTempMsgNeeded\":1,\"canton\":null,\"alterRuleId\":null}],\"couponTemplateId\":null,\"token\":null,\"t\":null,\"name\":null,\"sendType\":null,\"canton\":null,\"currentPage\":1,\"category\":1,\"startDate\":null,\"status\":2},\"success\":true}\n";
        String list = "[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司11\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升11\",\"dealerId\":\"610023\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江安澄商贸有限公司\",\"lmdt\":null,\"cityName\":\"宁波市江北区\",\"dealerShortName\":\"浙江安澄11\",\"dealerId\":\"610038\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江希雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州市瓯海区炬光园中路108号\",\"dealerShortName\":\"浙江希雅\",\"dealerId\":\"610040\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"温州晟雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州\",\"dealerShortName\":\"温州晟雅\",\"dealerId\":\"610067\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"中铁互联（杭州）供应链管理有限公司\",\"lmdt\":null,\"cityName\":\"杭州\",\"dealerShortName\":\"中铁互联\",\"dealerId\":\"630004\",\"delState\":\"\",\"orderState\":\"\"}]";
        String list2 = "[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江安澄商贸有限公司\",\"lmdt\":null,\"cityName\":\"宁波市江北区\",\"dealerShortName\":\"浙江安澄\",\"dealerId\":\"610038\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江希雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州市瓯海区炬光园中路108号\",\"dealerShortName\":\"浙江希雅\",\"dealerId\":\"610040\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"温州晟雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州\",\"dealerShortName\":\"温州晟雅\",\"dealerId\":\"610067\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"中铁互联（杭州）供应链管理有限公司\",\"lmdt\":null,\"cityName\":\"杭州\",\"dealerShortName\":\"中铁互联\",\"dealerId\":\"630004\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司11\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升11\",\"dealerId\":\"610023\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江安澄商贸有限公司\",\"lmdt\":null,\"cityName\":\"宁波市江北区\",\"dealerShortName\":\"浙江安澄11\",\"dealerId\":\"610038\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"浙江希雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州市瓯海区炬光园中路108号\",\"dealerShortName\":\"浙江希雅\",\"dealerId\":\"610040\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"温州晟雅化妆品有限公司\",\"lmdt\":null,\"cityName\":\"温州\",\"dealerShortName\":\"温州晟雅\",\"dealerId\":\"610067\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"中铁互联（杭州）供应链管理有限公司\",\"lmdt\":null,\"cityName\":\"杭州\",\"dealerShortName\":\"中铁互联\",\"dealerId\":\"630004\",\"delState\":\"\",\"orderState\":\"\"}]";

        String ignoreList1="[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\",\"delState\":\"\",\"orderState\":\"\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司22\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"delState\":\"\",\"orderState\":\"\"}]";
        String ignoreList2="[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-exp\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\",\"orderList\":[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-1-exp\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-2\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"}]},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司2\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"orderList\":[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司2-1\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司2-2\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"}]}]";
        String ignoreList3="[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-res\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\",\"orderList\":[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-1-res\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司1-2\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"}]},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"安徽省新大新化妆品有限公司2\",\"lmdt\":null,\"cityName\":\"蚌埠市\",\"dealerShortName\":\"安徽新大新\",\"dealerId\":\"610024\",\"orderList\":[{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司2-1\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"},{\"crnm\":null,\"synchTime\":null,\"crdt\":null,\"dealerName\":\"合肥人广利升贸易有限公司2-2\",\"lmdt\":null,\"cityName\":\"合肥\",\"dealerShortName\":\"合肥人广利升\",\"dealerId\":\"610023\"}]}]";











        System.out.println(JsonCompareUtil.jsonCompare(ignoreList2, ignoreList3));

        //System.out.println(JsonCompareUtil.jsonCompare(expect6, response6));

        //System.out.println(JsonCompareUtil.jsonCompare(response, expect));
        //        System.out.println(JsonCompareUtil.jsonCompare(responsenull, expect));
        //        System.out.println(JsonCompareUtil.jsonCompare(response1, expect1));
        //        System.out.println(JsonCompareUtil.jsonCompare(response2, expect2));
        //        System.out.println(JsonCompareUtil.jsonCompare(response3, expect3));


    }
}
