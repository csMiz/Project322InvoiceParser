package com.p322.recg.invoice_parser.web.controller;

import com.p322.recg.invoice_parser.web.domain.Constant;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class PageController {

    @ApiOperation(value = "homepage")
    @GetMapping("/invoice/home")
    public String getHomePage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "/static/home.html";
    }

    @ApiOperation(value = "resultPage")
    @GetMapping("/invoice/result")
    public String getResultPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "/static/result.html";
    }

    @ApiOperation(value = "historyPage")
    @GetMapping("/invoice/history")
    public String getHistoryPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "/static/history.html";
    }


    @GetMapping(value = "/image",produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String file_str = request.getParameter("file");
        File file = new File(Constant.path + file_str);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes, 0, inputStream.available());
        return bytes;
    }

}
