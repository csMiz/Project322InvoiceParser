var CurrentUser = "Admin";

function initializePage(){
    $(".top_username").html(CurrentUser);
    $(".device_page").hide();
    $(".single_device_page").hide();


}

$(document).ready(function() {
    initializePage();

    $(".left_glance_button").click(function(){
        jumpToHome();
    });
    $(".left_device_list_button").click(function(){
        jumpToHistory();
    });
    $('.history_grid').on('click','.box_record',function(){
        var html = $(this).children(".lbl_small_id").html();
        var args = html.split("：");
        var id = args[1];
        window.location.href = "./result?id=" + id;
    });

    getHistory20(0);


});


function getHistory20(page){
    var uri = "/invoice/history/get20";
    $.post(uri, {
        page: page
    }, function (data) {
        parseRecords(data);
    });
}

function parseRecords(data){
    var tmpFullHTML = "";
    for (var i in data){
        var tmpResult = parseOneRecord(data[i]);
        var code_t;
        if (data[i]["resultCode"] >= 64){
            code_t = 7;
        }else{
            code_t = data[i]["resultCode"];
        }
        var tmpRecord = "<div class='box_record result_code" + code_t + "'>" + tmpResult + "</div>";
        tmpFullHTML = tmpFullHTML + tmpRecord;
    }
    $(".history_grid").html(tmpFullHTML);

}

function parseOneRecord(data){
    var result_html = "查询时间：" + data["date"] + "<br/>" +
        "<div class='lbl_small_id'>记录号：" + data["id"] + "</div>" +
        "<strong>" + "查询结果：" + parseResultCode(data["resultCode"]) + "</strong>" +
        "价税合计：" + data["amountInFiguers"] + "<br/>" +
        "售方名称：" + data["sellerName"] + "<br/>" +
        "购方名称：" + data["purchaserName"] + "<br/>" +
        "发票类型：" + data["invoiceType"] + "<br/>" +
        "查询人员：" + "Admin";
    return result_html;
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
        return "识别已完成，部分信息不匹配，需要人工校核";
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

