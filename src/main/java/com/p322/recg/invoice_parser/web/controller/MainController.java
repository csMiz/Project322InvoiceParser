package com.p322.recg.invoice_parser.web.controller;

import com.p322.recg.invoice_parser.web.domain.Constant;
import com.p322.recg.invoice_parser.web.domain.InvoiceEntity;
import com.p322.recg.invoice_parser.web.domain.InvoiceVerify;
import com.p322.recg.invoice_parser.web.service.InvoiceService;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class MainController {

    @Autowired
    private InvoiceService invoiceService;

    @ApiOperation(value = "Upload image to recognise")
    @PostMapping("/invoice/upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("qrfile") MultipartFile qrfile, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.ok("File is EMPTY");
        }
        String id = request.getParameter("id");

        String fileName = file.getOriginalFilename();
        String filePath = Constant.path;
        File dest = new File(filePath + fileName);
        InvoiceEntity invoice = null;
        try {
            file.transferTo(dest);
            invoice = invoiceService.createInvoice(dest, id,false);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        if (!qrfile.isEmpty()) {
            String qrName = qrfile.getOriginalFilename();
            String qrPath = "C:/Users/asdfg/Desktop/code test/";
            File qr = new File(qrPath + qrName);
            try {
                qrfile.transferTo(qr);
                String qr_result = invoiceService.getQRResult(qr);
                invoice = invoiceService.verifyQR(qr_result, id);
            } catch (IOException e) {
                System.out.println(e.toString());
            }
            return ResponseEntity.ok(invoice);
        }
        return ResponseEntity.ok(invoice);
    }

    @ApiOperation(value = "Request new Invoice Task")
    @PostMapping("/invoice/request")
    @ResponseBody
    public ResponseEntity<?> applyInvoiceRecord(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = invoiceService.applyInvoiceRecord();
        return ResponseEntity.ok(id);

    }

    @ApiOperation(value = "Search for one result")
    @PostMapping("/invoice/record")
    @ResponseBody
    public ResponseEntity<?> getInvoiceRecordFromId(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        InvoiceEntity invoiceEntity = invoiceService.getInvoiceEntityFromId(id);
        if (invoiceEntity == null){
            return ResponseEntity.ok("");
        }
        return ResponseEntity.ok(invoiceEntity);
    }

    @ApiOperation(value = "Search for one result of verification")
    @PostMapping("/invoice/vrecord")
    @ResponseBody
    public ResponseEntity<?> getInvoiceVerificationFromId(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        InvoiceVerify invoiceVerify = invoiceService.getInvoiceVerifyRecordFromId(id);
        if (invoiceVerify == null){
            return ResponseEntity.ok("");
        }
        return ResponseEntity.ok(invoiceVerify);
    }

    @ApiOperation(value = "")
    @PostMapping("/invoice/history/get20")
    @ResponseBody
    public ResponseEntity<?> getAdminHistory20(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String page = request.getParameter("page");
        List<InvoiceEntity> invoiceEntities = invoiceService.getAdminEntityFromPage(Integer.parseInt(page));
        return ResponseEntity.ok(invoiceEntities);
    }

    @ApiOperation(value = "")
    @PostMapping("/invoice/checkupload")
    @ResponseBody
    public ResponseEntity<?> getUploadStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        InvoiceEntity invoiceEntity = invoiceService.getInvoiceEntityFromId(id);
        if (invoiceEntity == null){
            return ResponseEntity.ok("null");
        }
        String imgref = invoiceEntity.getImageRef();
        if (imgref == null){
            return ResponseEntity.ok("null");
        }
        if ("".equals(imgref)){
            return ResponseEntity.ok("null");
        }
        return ResponseEntity.ok(imgref);
    }

}
