package com.p322.recg.invoice_parser.web.domain;


import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface InvoiceVerifyRepository extends PagingAndSortingRepository<InvoiceVerify,String> {

    InvoiceVerify getOne(String id);

    List<InvoiceVerify> findByOrderById();

    InvoiceVerify findByHeadJYM(String jym);


}