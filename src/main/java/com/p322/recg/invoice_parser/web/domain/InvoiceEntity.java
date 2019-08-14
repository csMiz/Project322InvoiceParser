package com.p322.recg.invoice_parser.web.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *  发票信息实体类
 *
 *
 * */

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tbl_invoice")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
public class InvoiceEntity {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "date", nullable = false)
    private Timestamp date;

    @Column(name = "result", nullable = false)
    private Integer resultCode;

    @Column(name = "user")
    private String user;

    @Column(name = "image_ref")
    private String imageRef;

    @Column(name = "invoice_code")
    private String invoiceCode;

    @Column(name = "invoice_num")
    private String invoiceNum;

    @Column(name = "invoice_date")
    private String invoiceDate;

    @Column(name = "total_amount")
    private String totalAmount;

    @Column(name = "amount_in_figuers")
    private String amountInFiguers;

    @Column(name = "seller_name")
    private String sellerName;

    @Column(name = "seller_reg_num")
    private String sellerRegisterNum;

    @Column(name = "purchaser_name")
    private String purchaserName;

    @Column(name = "purchaser_reg_num")
    private String purchaserRegisterNum;

    @Column(name = "invoice_type")
    private String invoiceType;

    @Column(name = "check_code")
    private String checkCode;



}
