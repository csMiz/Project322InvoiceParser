var CurrentUser = "Admin";
var searchId = "";
var glancePageTimer;
var repeatTime = 0;
var slowTimer;
var slowRepeat = 0;
var searchResult = "";

function initializePage(){
    $(".top_username").html(CurrentUser);
    $(".device_page").hide();
    $(".single_device_page").hide();
    $(".result_grid").html("处理中...");
    repeatTime = 0;
}

function getQueryVariable(variable)
{
    var query = window.location.search.substring(1);
    var vars = query.split("&");
    for (var i=0;i<vars.length;i++) {
        var pair = vars[i].split("=");
        if(pair[0] === variable){return pair[1];}
    }
    return(false);
}

$(document).ready(function() {
    initializePage();

    $(".left_glance_button").click(function(){
        jumpToHome();
    });
    $(".left_device_list_button").click(function(){
        jumpToHistory();
    });

    searchId = getQueryVariable("id");
    $(".result_grid").html("记录号：" + searchId + "<br/>");
    // glancePageTimer = setInterval(getGlanceDeviceList,1000);
    getGlanceDeviceList();

});

function getGlanceDeviceList(){
    // repeatTime += 1;
    // if (repeatTime > 2){
    //     slowTimer = setInterval(refreshResult(),1000);
    //     clearInterval(glancePageTimer);
    // }
    var result1 = "处理中...";
    var result2 = "";
    var result3 = "";
    var uri = "/invoice/record";
    $.post(uri, {
        id: searchId
    }, function (data) {
        var code_str = parseResultCode(data["resultCode"]);
        var code_t;
        if (data["resultCode"] >= 64){
            code_t = 7;
        }else{
            code_t = data["resultCode"];
        }
        result1 = "<div class='lbl_restype'>扫描识别结果：</div>" +
            "记录号：" + searchId + "<br/>" +
            "查询时间：" + data["date"] + "<br/>" +
            "<div class='lbl_res" + code_t + "'><strong>" + "查询结果：" + code_str + "</strong></div>" +
            "发票代码：" + data["invoiceCode"] + "<br/>" +
            "发票号码：" + data["invoiceNum"] + "<br/>" +
            "发票日期：" + data["invoiceDate"] + "<br/>" +
            "总金额：" + data["totalAmount"] + "<br/>" +
            "价税合计：" + data["amountInFiguers"] + "<br/>" +
            "售方名称：" + data["sellerName"] + "<br/>" +
            "售方号码：" + data["sellerRegisterNum"] + "<br/>" +
            "购方名称：" + data["purchaserName"] + "<br/>" +
            "购方号码：" + data["purchaserRegisterNum"] + "<br/>" +
            "发票类型：" + data["invoiceType"] + "<br/>" +
            "校验码：" + data["checkCode"] + "<br/>" +
            "查询人员：" + "Admin" + "<br/><br/>";

        result3 = "<img class='img_res' src='../image?file=" + data["imageRef"] + "' />";

        searchResult = result1 + result2 + result3;
        $(".result_grid").html(searchResult);

        if (code_t == 0){
            setTimeout(function(){
                getGlanceDeviceList();
                },3000);
        }
    });

    var uri2 = "/invoice/vrecord";
    $.post(uri2, {
        id: searchId
    }, function (data) {
        if (data === ""){
            result2 = "<br/>无联网验证信息";
            searchResult = result1 + result2 + result3;
            $(".result_grid").html(searchResult);
            return;
        }
        result2 = "<div class='lbl_restype' >联网验证结果：</div>" +
            "校验码：" + data["jym"] + "<br/>" +
            "购方名称：" + data["gfmc"] + "<br/>" +
            "发票号码：" + data["fphm"] + "<br/>" +
            "发票代码：" + data["fpdm"] + "<br/>" +
            "金额：" + data["je"] + "<br/>" +
            "税额：" + data["se"] + "<br/>" +
            "价税合计：" + data["jshj"] + "<br/>" +
            "售方名称：" + data["xfmc"] + "<br/>" +
            "开票日期：" + data["kprq"] + "<br/>" +
            "购方纳税号：" + data["gfsbh"] + "<br/>" +
            "售方纳税号：" + data["xfsbh"] + "<br/>" +
            "联网查询信息：" + data["msg"];

        searchResult = result1 + result2 + result3;
        $(".result_grid").html(searchResult);
    });

}

function refreshResult(){
    slowRepeat += 1;
    $(".result_grid").html(searchResult);
    if (slowRepeat > 29){
        clearInterval(slowTimer);
    }
}

function parseResultCode(code_o){
    var code;
    if (code_o >= 64){
        code = 7;
    }else{
        code = code_o;
    }
    if (code == 0){
        return "识别处理中...";
    }
    if (code == 1){
        return "校核不通过";
    }
    if (code == 2){
        return "识别无结果";
    }
    if (code == 3){
        return "识别不完整";
    }
    if (code == 4){
        return "仅识别完成";
    }
    if (code == 5){
        return "识别已完成，但未查到记录";
    }
    if (code == 6){
        return "识别已完成，联网校验失败";
    }
    if (code == 7){
        var r = "识别已完成，部分信息不匹配，需要人工校核";
        if (code_o == 64){
            r = r + "（购方名称有误）";
        }
        return r;
    }
    if (code == 8){
        return "校核全部通过";
    }
    if (code == 9){
        return "识别已完成，联网校验'服务剩余次数不足，请再次购买'";
    }

}

function jumpToHome(){
    window.location.href = "./home";
}
function jumpToHistory(){
    window.location.href = "./history";
}

