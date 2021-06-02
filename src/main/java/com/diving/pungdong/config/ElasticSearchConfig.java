package com.diving.pungdong.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.diving.pungdong.repo.elasticSearch")
public class ElasticSearchConfig {
    @Bean
    public RestHighLevelClient client() {
        return new RestHighLevelClient(
                RestClient.builder(HttpHost.create("https://search-pungdong-lecture-byx4ncvp46qtssndxinvzwbgwa.ap-northeast-2.es.amazonaws.com:443")));
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(client());
    }
}
