package com.open.coinnews.web.api;

import com.open.coinnews.app.elasticsearch.EsNews;
import com.open.coinnews.app.elasticsearch.service.INewsEsService;
import com.open.coinnews.app.model.News;
import com.open.coinnews.app.service.INewsService;
import com.open.coinnews.utils.JsonUtil;
import com.open.coinnews.utils.ResultObject;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;

@RestController
@RequestMapping(value = "api")
public class ApiController {

    @Autowired
    private INewsEsService newsEsService;

    @Autowired
    private INewsService newsService;


    @GetMapping(value = "getNewList/{page}/{size}")
    public Object getNewsList(@PathVariable String page,@PathVariable String size,HttpServletRequest request, HttpServletResponse response){

        ResultObject resultObject = new ResultObject();

        Pageable pageable = PageRequest.of(Integer.valueOf(page),Integer.valueOf(size),new Sort(Sort.Direction.DESC,"createDate"));
        //先从es中查询结果集
        Page<EsNews> users = newsEsService.findAll(pageable);

        resultObject.setData(users);
        return JsonUtil.toJSONString(resultObject);
    }

    @GetMapping(value = "init")
    public Object init(HttpServletRequest request, HttpServletResponse response){

        ResultObject resultObject = new ResultObject();

        //spring-data-elasticsearch 集成的用法跟jpa方法相似
        //从库中查出所有数据添加到es
        List<News> news = newsService.findAll();
        List<EsNews> esnews = new ArrayList<>();
        news.stream().forEach(ne ->{
            EsNews es = new EsNews();
            BeanUtils.copyProperties(ne,es);
            esnews.add(es);
        });

        newsEsService.saveAll(esnews);

        return JsonUtil.toJSONString(resultObject);
    }

    @GetMapping(value = "getAll/{page}/{size}/{title}")
    public Object getAll(@PathVariable String page,@PathVariable String size,@PathVariable String title){

        ResultObject resultObject = new ResultObject();

      Pageable pageable = PageRequest.of(Integer.valueOf(page),Integer.valueOf(size),new Sort(Sort.Direction.DESC,"createDate"));
        /* Page<EsNews> users = newsEsService.findAll(pageable);*/

        //全文搜索关键字：即匹配相关度排序，时间排序
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(title)).withPageable(pageable).build();

        // 某字段按字符串模糊查询
        SearchQuery searchQuerys = new NativeSearchQueryBuilder().withQuery(matchQuery("content", title)).withPageable(pageable).build();

        // multi_match多个字段匹配某字符串
        SearchQuery searchQueryss = new NativeSearchQueryBuilder().withQuery(multiMatchQuery(title, "title", "content","mdContent")).withPageable(pageable).build();

        //完全包含查询 例如：查询‘我是’只查询出包含我是得结果
        //SearchQuery searchQueryq = new NativeSearchQueryBuilder().withQuery(matchQuery("title", title).operator(MatchQueryBuilder.Operator.AND)).build();

        //即boolQuery，可以设置多个条件的查询方式。它的作用是用来组合多个Query，有四种方式来组合，must，mustnot，filter，should。
        //must代表返回的文档必须满足must子句的条件，会参与计算分值；
        //filter代表返回的文档必须满足filter子句的条件，但不会参与计算分值；
        //should代表返回的文档可能满足should子句的条件，也可能不满足，有多个should时满足任何一个就可以，通过minimum_should_match设置至少满足几个。
        //mustnot代表必须不满足子句的条件。
        //譬如我想查询title包含“XXX”，且userId=“1”，且weight最好小于5的结果。
        Integer userId = 0;
        Integer weight = 0;
        SearchQuery searchQueryw = new NativeSearchQueryBuilder().withQuery(boolQuery().must(termQuery("userId", userId))
                .should(rangeQuery("weight").lt(weight)).must(matchQuery("title", title))).build();

        //如果某个字段需要匹配多个值，譬如userId为1，2，3任何一个的，类似于mysql中的in，那么可以使用termsQuery("userId", ids).

        //参考：https://blog.csdn.net/tianyaleixiaowu/article/details/77965257
        Page<EsNews> users = newsEsService.search(searchQuery);

        resultObject.setData(users);
        return JsonUtil.toJSONString(resultObject);
    }
}
