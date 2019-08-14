package com.p322.recg.invoice_parser.web.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tbl_invoice_verify")
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
public class InvoiceVerify {


    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "result", nullable = false)
    private String resultCode;

    @Column(name = "msg")
    private String msg;

    @Column(name = "jym")
    private String JYM;

    @Column(name = "gfmc")
    private String GFMC;

    @Column(name = "cysj")
    private String verification_date;

    @Column(name = "fphm")
    private String FPHM;

    @Column(name = "se")
    private String SE;

    @Column(name = "fpdm")
    private String FPDM;

    @Column(name = "cycs")
    private String CYCS;

    @Column(name = "xfdzdh")
    private String XFDZDH;

    @Column(name = "jshj")
    private String JSHJ;

    @Column(name = "xfmc")
    private String XFMC;

    @Column(name = "detail")
    private String detail;

    @Column(name = "gfsbh")
    private String GFSBH;

    @Column(name = "xfyhzh")
    private String XFYHZH;

    @Column(name = "jqbh")
    private String JQBH;

    @Column(name = "kprq")
    private String KPRQ;

    @Column(name = "je")
    private String JE;

    @Column(name = "xfsbh")
    private String XFSBH;

    @Column(name = "h_jym")
    private String headJYM;

    @Column(name = "h_fphm")
    private String head_FPHM;

    @Column(name = "h_fplx")
    private String head_FPLX;

    @Column(name = "h_fpdm")
    private String head_FPDM;

    @Column(name = "h_fpje")
    private String head_FPJE;

    @Column(name = "h_cyjgdm")
    private String head_CYJGDM;

    @Column(name = "h_kprq")
    private String head_KPRQ;
}
