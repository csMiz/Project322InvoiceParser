var CurrentUser = "Admin";
var uploadTimer;

function initializePage(){
    $(".top_username").html(CurrentUser);
    $(".device_page").hide();
    $(".single_device_page").hide();
}

$(document).ready(function() {
    initializePage();

    $(".chooseFile_button").click(function(){
        selectFile();
    });
    $(".chooseQR_button").click(function(){
        selectQR();
    });
    $(".uploadFile_button").click(function(){
        uploadFile();
    });
    $(".left_glance_button").click(function(){
        jumpToHome();
    });
    $(".left_device_list_button").click(function(){
        jumpToHistory();
    });

});

function selectFile(){
    $(".chooseFile_input").click();
}
function selectQR(){
    $(".chooseQR_input").click();
}
function uploadFile(){
    var uri = "/invoice/request";
    $.post(uri, {
        content: ""
    }, function (data) {
        $(".upload_label").html("Processing...");
        $(".upload_invoice_id").val(data);
        $(".uploadFile_form").submit();

        uploadTimer = setInterval(function(){
            $.post("/invoice/checkupload", {
                id: data
            }, function (data2) {
                if (data2 === "null"){
                    return;
                }
                clearInterval(uploadTimer);
                window.location.href = "./result?id=" + data;
            });
        }, 1000);

        // $(".uploadFile_button").html("<b>...</b>").css("text-indent","100px");


    });

    // 申请一个查询号
    // 识别完成后自动填入，同时前端监视此查询号
}
function jumpToHome(){
    window.location.href = "./home";
}
function jumpToHistory(){
    window.location.href = "./history";
}

