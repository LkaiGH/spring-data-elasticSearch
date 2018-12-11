package com.open.coinnews.app.service;

import com.open.coinnews.app.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface INewsService extends JpaRepository<News, Integer>, JpaSpecificationExecutor<News> {

}
