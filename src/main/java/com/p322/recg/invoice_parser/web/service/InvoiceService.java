package com.p322.recg.invoice_parser.web.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.p322.recg.invoice_parser.web.domain.*;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.core.support.PersistenceExceptionTranslationRepositoryProxyPostProcessor;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoiceVerifyRepository invoiceVerifyRepository;

    /**
     *  获取百度识图api的token
     *  需要提供百度账号凭证ak和sk
     *
     *
     *
     * */
    private String getBaiduAuth(String ak, String sk) {
        // 获取token地址
        String authHost = "https://aip.baidubce.com/oauth/2.0/token?";
        String getAccessTokenUrl = authHost
                // 1. grant_type为固定参数
                + "grant_type=client_credentials"
                // 2. 官网获取的 API Key
                + "&client_id=" + ak
                // 3. 官网获取的 Secret Key
                + "&client_secret=" + sk;
        try {
            URL realUrl = new URL(getAccessTokenUrl);
            // 打开和URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.err.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String result = "";
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            /**
             * 返回结果示例
             */
            System.err.println("result:" + result);
            JSONObject jsonObject = new JSONObject(result);
            String access_token = jsonObject.getString("access_token");
            return access_token;
        } catch (Exception e) {
            System.err.printf("获取token失败！");
            e.printStackTrace(System.err);
        }
        return null;
    }

    /**
     *  使用百度api识图
     *  返回Json结果
     *
     * */
    public JSONObject getInvoiceResult(File file) throws Exception{
        String baidu_token;
        baidu_token = getBaiduAuth(Constant.ak,Constant.sk);
        String api_url_string = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice?access_token=" + baidu_token;
        String quality = "normal";
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        Integer len = stream.available();
        byte[] bytes = new byte[len];
        stream.read(bytes, 0, len);
        stream.close();
        byte[] encodeBase64 = Base64.encodeBase64(bytes);
        String img_str = new String(encodeBase64);

        URL api_url = new URL(api_url_string);
        HttpURLConnection connection = (HttpURLConnection)api_url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

        connection.connect();
        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());

        String ss = "image=" + URLEncoder.encode(img_str, "UTF-8") + "&accuracy=" + quality;
        out.writeBytes(ss);
        out.flush();
        out.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = "";
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }

        return new JSONObject(result);
    }


    /**
     * 发票校核主程序
     *
     *
     * */
    public InvoiceEntity createInvoice(File file, String id, boolean ifVerify) throws Exception{
        InvoiceEntity invoiceEntity = invoiceRepository.getOne(id);
        JSONObject jsonObject = getInvoiceResult(file);

        Set set = jsonObject.keySet();
        if (!set.contains("words_result")){
            saveEmptyInvoiceRecord(invoiceEntity, file.getName());
            return null;
        }

        JSONObject jsonObject2 = jsonObject.getJSONObject("words_result");
        String invoice_code = "";
        String invoice_num = "";
        String invoice_date = "";
        String total_amount = "";
        String amount_in_figuers = "";
        String seller_name = "";
        String seller_reg_num = "";
        String purchaser_name = "";
        String purchaser_reg_num = "";
        String invoice_type = "";

        String check_code = "";

        boolean property_missing = false;
        try{
            invoice_code = jsonObject2.getString("InvoiceCode");
            invoice_num = jsonObject2.getString("InvoiceNum");
            invoice_date = jsonObject2.getString("InvoiceDate");
            total_amount = jsonObject2.getString("TotalAmount");
            amount_in_figuers = jsonObject2.getString("AmountInFiguers");
            seller_name = jsonObject2.getString("SellerName");
            seller_reg_num = jsonObject2.getString("SellerRegisterNum");
            purchaser_name = jsonObject2.getString("PurchaserName");
            purchaser_reg_num = jsonObject2.getString("PurchaserRegisterNum");
            invoice_type = jsonObject2.getString("InvoiceType");
        }catch (Exception ex){
            property_missing = true;
        }

        Integer resultCode = 4;
        if (property_missing) {
            resultCode = 3;
        }

        invoiceEntity.setImageRef(file.getName());
        invoiceEntity.setResultCode(resultCode);
        invoiceEntity.setInvoiceCode(invoice_code);
        invoiceEntity.setInvoiceNum(invoice_num);
        invoiceEntity.setInvoiceDate(invoice_date);
        invoiceEntity.setTotalAmount(total_amount);
        invoiceEntity.setAmountInFiguers(amount_in_figuers);
        invoiceEntity.setSellerName(seller_name);
        invoiceEntity.setSellerRegisterNum(seller_reg_num);
        invoiceEntity.setPurchaserName(purchaser_name);
        invoiceEntity.setPurchaserRegisterNum(purchaser_reg_num);
        invoiceEntity.setInvoiceType(invoice_type);

        try{
            check_code = jsonObject2.getString("CheckCode");
        }catch (Exception ex){
        }
        invoiceEntity.setCheckCode(check_code);

        invoiceRepository.save(invoiceEntity);

        selfVerify(invoiceEntity);

        if (ifVerify){
            if (!"".equals(check_code)){
                verifyCode(invoiceEntity);
            }
        }

        return invoiceEntity;

    }

    private void saveEmptyInvoiceRecord(InvoiceEntity invoice, String imgRef){
        invoice.setImageRef(imgRef);
        invoice.setResultCode(2);
        invoiceRepository.save(invoice);

    }

    public String applyInvoiceRecord(){
        Timestamp date = new Timestamp(System.currentTimeMillis()); //获取当前时间
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        InvoiceEntity tmpInvoice = InvoiceEntity.builder()
                .id(uuid)
                .imageRef("")
                .resultCode(0)
                .date(date)
                .build();
        invoiceRepository.save(tmpInvoice);
        return uuid;
    }

    public InvoiceEntity getInvoiceEntityFromId(String id){
        return invoiceRepository.getOne(id);
    }

    public InvoiceVerify getInvoiceVerifyRecordFromId(String id){
        return invoiceVerifyRepository.getOne(id);
    }

    public List<InvoiceEntity> getAdminEntityFromPage(Integer page){
        Pageable pageRequest = new PageRequest(page, 20);
        return invoiceRepository.findByOrderByDateDesc(pageRequest);
    }

    public String deEncodeByBase64(String baseStr) {
        String content = null;
        BufferedImage image;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b=null;
        try {
//            int i = baseStr.indexOf("data:image/png;base64,");
//            baseStr = baseStr.substring(i+"data:image/png;base64,".length());//去掉base64图片的data:image/png;base64,部分才能转换为byte[]

            b = decoder.decodeBuffer(baseStr);//baseStr转byte[]
            ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(b);//byte[] 转BufferedImage
            image = ImageIO.read(byteArrayInputStream);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);//解码
            System.out.println("图片中内容：  ");
            System.out.println("content： " + result.getText());
            content = result.getText();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return content;
    }

    public String getQRResult(File file) throws Exception {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file));
        Integer len = stream.available();
        byte[] bytes = new byte[len];
        stream.read(bytes, 0, len);
        byte[] encodeBase64 = Base64.encodeBase64(bytes);
        String img_str = new String(encodeBase64);
        return deEncodeByBase64(img_str);
    }

    public InvoiceEntity verifyQR(String qr_info, String id) throws Exception {

        System.out.println(qr_info);
        return null;
    }

    public InvoiceEntity verifyCode(InvoiceEntity invoice) throws Exception {

        String verifyCode = "";
        if (invoice.getCheckCode().length() > 6){
            verifyCode = invoice.getCheckCode().substring(invoice.getCheckCode().length()-6);
        }else{
            return invoice;
        }

        InvoiceVerify invoiceVerify = null;

        InvoiceVerify historyVerification = invoiceVerifyRepository.findByHeadJYM(verifyCode);
        boolean hasHistory = false;
        if (!(historyVerification == null)){
            hasHistory = true;
            if (YonyouService.getInvoiceDateCode(invoice.getInvoiceDate()).equals(historyVerification.getHead_KPRQ())){
                hasHistory = false;
            }
            if (invoice.getInvoiceNum().equals(historyVerification.getHead_FPHM())){
                hasHistory = false;
            }
            if (invoice.getInvoiceCode().equals(historyVerification.getHead_FPDM())){
                hasHistory = false;
            }
        }

        if (!hasHistory){

            String result = YonyouService.getYonYouRequest(YonyouService.getInvoiceDateCode(invoice.getInvoiceDate()),
                    invoice.getInvoiceNum(),
                    invoice.getInvoiceCode(),
                    verifyCode,
                    invoice.getTotalAmount(),
                    YonyouService.getInvoiceTypeCode(invoice.getInvoiceType()));

//            String result = "{\"Data\":\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?><MSG><BODY>" +
//                    "<JYM>78980422053167398650</JYM>" +
//                    "<GFMC>杭州申昊科技股份有限公司</GFMC>" +
//                    "<CYSJ>2019-08-13 14:17:02</CYSJ>" +
//                    "<FPHM>00770321</FPHM>" +
//                    "<SE>6.12</SE>" +
//                    "<FPDM>033001900104</FPDM>" +
//                    "<CYCS>1</CYCS>" +
//                    "<XFDZDH>杭州市余杭区仓前街道仓兴街66、68-1号 15306507680</XFDZDH>" +
//                    "<JSHJ>210.00</JSHJ>" +
//                    "<XFMC>杭州余杭区仓前街道小菲餐馆</XFMC>" +
//                    "<CHILDLIST><CHILD><SE>6.12</SE> <SL></SL> <GGXH></GGXH> <JE>203.88</JE> <HWMC>*餐饮服务*餐费</HWMC> <SLV>3</SLV> <DW></DW> <DJ></DJ> </CHILD></CHILDLIST>" +
//                    "<GFSBH>91330100742929345R</GFSBH>" +
//                    "<ZFBZ>N</ZFBZ>" +
//                    "<BZ></BZ>" +
//                    "<XFYHZH>浙江杭州余杭农村商业银行股份有限公司仓前支行201000211844170</XFYHZH>" +
//                    "<JQBH>661614522549</JQBH>" +
//                    " <KPRQ>20190730</KPRQ>" +
//                    "<CPYBZ>N</CPYBZ>" +
//                    "<GFDZDH></GFDZDH>" +
//                    "<GFYHZH></GFYHZH>" +
//                    "<JE>203.88</JE>" +
//                    "<XFSBH>92330110MA2BR6ET0Q</XFSBH> </BODY>" +
//                    "<HEAD><JYM>398650</JYM> <FPHM>00770321</FPHM> <FPLX>04</FPLX> <FPDM>033001900104</FPDM> <FPJE>203.88</FPJE> <CYJGDM>001</CYJGDM> <KPRQ>20190730</KPRQ> </HEAD>" +
//                    "</MSG>\",\"Msg\":\"发票查验操作成功\",\"Code\":\"0000\"}";

            JSONObject jsonObject = new JSONObject(result);
            String r_code = jsonObject.getString("Code");
            String r_msg = jsonObject.getString("Msg");
            String r_body = jsonObject.getString("Data");

            invoiceVerify = InvoiceVerify.builder()
                    .id(invoice.getId())
                    .resultCode(r_code)
                    .msg(r_msg)
                    .build();

            if (!"0000".equals(r_code)){
                invoiceVerifyRepository.save(invoiceVerify);
                if (Integer.parseInt(r_code) >= 30000){
                    invoice.setResultCode(9);
                }else{
                    invoice.setResultCode(6);
                }
                invoiceRepository.save(invoice);
                return invoice;
            }

            String str_pattern1 = "<JYM>(.*)</JYM>.*<GFMC>(.*)</GFMC>.*<CYSJ>(.*)</CYSJ>.*" +
                    "<FPHM>(.*)</FPHM>.*<SE>(.*)</SE>.*<FPDM>(.*)</FPDM>.*" +
                    "<CYCS>(.*)</CYCS>.*<XFDZDH>(.*)</XFDZDH>.*<JSHJ>(.*)</JSHJ>.*" +
                    "<XFMC>(.*)</XFMC>.*<CHILDLIST>(.*)</CHILDLIST>";
            Pattern pattern1 = Pattern.compile(str_pattern1);
            Matcher matcher1 = pattern1.matcher(r_body);
            if (matcher1.find()){
                invoiceVerify.setJYM(matcher1.group(1));
                invoiceVerify.setGFMC(matcher1.group(2));
                invoiceVerify.setVerification_date(matcher1.group(3));
                invoiceVerify.setFPHM(matcher1.group(4));
                invoiceVerify.setSE(matcher1.group(5));
                invoiceVerify.setFPDM(matcher1.group(6));
                invoiceVerify.setCYCS(matcher1.group(7));
                invoiceVerify.setXFDZDH(matcher1.group(8));
                invoiceVerify.setJSHJ(matcher1.group(9));
                invoiceVerify.setXFMC(matcher1.group(10));
                invoiceVerify.setDetail(matcher1.group(11));
            }

            String str_pattern2 = "<GFSBH>(.*)</GFSBH>.*<ZFBZ>(.*)</ZFBZ>.*" +
                    "<XFYHZH>(.*)</XFYHZH>.*<JQBH>(.*)</JQBH>.*<KPRQ>(.*)</KPRQ>.*" +
                    "<CPYBZ>(.*)</CPYBZ>.*<JE>(.*)</JE>.*<XFSBH>(.*)</XFSBH>";
            Pattern pattern2 = Pattern.compile(str_pattern2);
            Matcher matcher2 = pattern2.matcher(r_body);
            if (matcher2.find()){
                invoiceVerify.setGFSBH(matcher2.group(1));
                invoiceVerify.setXFYHZH(matcher2.group(3));
                invoiceVerify.setJQBH(matcher2.group(4));
                invoiceVerify.setKPRQ(matcher2.group(5));
                invoiceVerify.setJE(matcher2.group(7));
                invoiceVerify.setXFSBH(matcher2.group(8));
            }

            String str_pattern3 = "<HEAD>.*<JYM>(.*)</JYM>.*<FPHM>(.*)</FPHM>.*" +
                    "<FPLX>(.*)</FPLX>.*<FPDM>(.*)</FPDM>.*<FPJE>(.*)</FPJE>.*" +
                    "<CYJGDM>(.*)</CYJGDM>.*<KPRQ>(.*)</KPRQ>.*</HEAD>";
            Pattern pattern3 = Pattern.compile(str_pattern3);
            Matcher matcher3 = pattern3.matcher(r_body);
            if (matcher3.find()){
                invoiceVerify.setHeadJYM(matcher3.group(1));
                invoiceVerify.setHead_FPHM(matcher3.group(2));
                invoiceVerify.setHead_FPLX(matcher3.group(3));
                invoiceVerify.setHead_FPDM(matcher3.group(4));
                invoiceVerify.setHead_FPJE(matcher3.group(5));
                invoiceVerify.setHead_CYJGDM(matcher3.group(6));
                invoiceVerify.setHead_KPRQ(matcher3.group(7));
            }

        }
        else{   // has history

            invoiceVerify = historyVerification;

        }

        invoice.setResultCode(1);

        boolean all_match = true;
        String recg_invoice_num = invoice.getInvoiceNum();
        if (!invoiceVerify.getHead_FPHM().equals(recg_invoice_num)){
            all_match = false;
        }
        if (!invoiceVerify.getHead_FPLX().equals(YonyouService.getInvoiceTypeCode(invoice.getInvoiceType()))){
            all_match = false;
        }
        if (!invoiceVerify.getHead_FPDM().equals(invoice.getInvoiceCode())){
            all_match = false;
        }
        if (!invoiceVerify.getHead_FPJE().equals(invoice.getTotalAmount())){
            all_match = false;
        }
        if (!invoiceVerify.getHead_KPRQ().equals(YonyouService.getInvoiceDateCode(invoice.getInvoiceDate()))){
            all_match = false;
        }


        if (all_match){
            invoice.setResultCode(7);

            boolean strict = true;
            if (!invoiceVerify.getGFMC().equals(invoice.getPurchaserName())){
                strict = false;
//            invoice.setResultCode(64);
            }

            if (strict){
                invoice.setResultCode(8);
            }
        }

        invoiceVerifyRepository.save(invoiceVerify);
        invoiceRepository.save(invoice);
        return invoice;
    }

    public InvoiceEntity selfVerify(InvoiceEntity invoice){

        Integer checkCodeLength = invoice.getCheckCode().length();
        if (!(checkCodeLength == 0) && !(checkCodeLength == 20)){
            invoice.setResultCode(1);
        }

        invoiceRepository.save(invoice);
        return invoice;
    }



}
