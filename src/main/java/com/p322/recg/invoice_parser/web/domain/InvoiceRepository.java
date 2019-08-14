package com.p322.recg.invoice_parser.web.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface InvoiceRepository extends PagingAndSortingRepository<InvoiceEntity,String> {

    InvoiceEntity getOne(String id);

    List<InvoiceEntity> findByOrderById();

    List<InvoiceEntity> findByOrderByDateDesc(Pageable pageable);

}
