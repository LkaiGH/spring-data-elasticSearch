package com.open.coinnews.app.elasticsearch.service;

import com.open.coinnews.app.elasticsearch.EsNews;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface INewsEsService extends ElasticsearchRepository<EsNews,Integer> {

}
