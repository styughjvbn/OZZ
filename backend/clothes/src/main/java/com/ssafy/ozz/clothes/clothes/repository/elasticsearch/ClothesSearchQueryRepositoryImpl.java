package com.ssafy.ozz.clothes.clothes.repository.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.ssafy.ozz.clothes.clothes.domain.ClothesDocument;
import com.ssafy.ozz.clothes.clothes.dto.request.ClothesSearchCondition;
import com.ssafy.ozz.clothes.clothes.dto.request.VectorRequest;
import com.ssafy.ozz.clothes.clothes.dto.response.VectorResponse;
import com.ssafy.ozz.clothes.global.es.Indices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.RefreshPolicy;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClothesSearchQueryRepositoryImpl implements ClothesSearchQueryRepository {

    private final ElasticsearchOperations operations;
    private final WebClient webClient;

    public ClothesSearchQueryRepositoryImpl(ElasticsearchOperations operations, WebClient webClient) {
        operations.withRefreshPolicy(RefreshPolicy.WAIT_UNTIL);
        this.operations = operations;
        this.webClient = webClient;
    }

    public float[] getVector(String text) {
        return Objects.requireNonNull(webClient.post()
                .uri("/vectorize")
                .bodyValue(VectorRequest.of(text))
                .retrieve()
                .bodyToMono(VectorResponse.class).block()).vector();
    }

    @Override
    public Page<ClothesDocument> findByCondition(ClothesSearchCondition condition, Pageable pageable) {
        return findByCondition(null,condition, pageable);
    }


    @Override
    public Page<ClothesDocument> findByCondition(Long userId, ClothesSearchCondition condition, Pageable pageable) {
//        Query query = createConditionNativeQuery(condition,pageable);
        Query query = createConditionNativeQuery(userId,condition,pageable);

        SearchHits<ClothesDocument> searchHits = operations.search(query, ClothesDocument.class);

        List<ClothesDocument> documents = searchHits
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(documents, pageable, searchHits.getTotalHits());
    }

    @Override
    public void update(ClothesDocument document) {
        Document updateDocument  = operations.getElasticsearchConverter().mapObject(document);

        operations.update(UpdateQuery.builder(document.getId())
            .withDocument(updateDocument)
            .withDocAsUpsert(true)
            .build(), IndexCoordinates.of(Indices.CLOTHES_INDEX));
    }

    private Query createConditionNativeQuery(Long userId, ClothesSearchCondition condition, Pageable pageable) {
        // Split search term into tokens
        return NativeQuery.builder()
                .withQuery(q->q
                    .bool(b->{
                        matchKeyword(b,condition.keyword());
                        filterUser(b,userId);
                        filterCategoryLow(b,condition.categoryLowId());
                        filterCategoryHigh(b,condition.categoryHighId());
                        return b;
                    })
                )
                .withPageable(pageable) // Pagination
                .build();
    }

    private BoolQuery.Builder matchKeyword(BoolQuery.Builder b, String keyword) {
        if (keyword == null || keyword.isEmpty()) return b;
        b.must(m -> m
                .match(mm -> mm
                        .field("name")
                        .query(keyword)
                )
        )
        .should(s -> s
                .matchPhrase(mp -> mp
                        .field("name")
                        .query(keyword)
                )
        );

        String[] tokens = keyword.split("\\s+");
        for (String token : tokens) {
            b.should(s -> s
                    .term(t -> t
                            .field("name")
                            .value(token)
                    )
            );
        }
        return b;
    }

    private BoolQuery.Builder filterCategoryLow(BoolQuery.Builder b, Byte categoryLowId) {
        if (categoryLowId == null) return b;
        return b.filter(f -> f
                .term(t -> t
                        .field("category_low_id")
                        .value(categoryLowId)
                )
        );
    }

    private BoolQuery.Builder filterCategoryHigh(BoolQuery.Builder b, Byte categoryHighId) {
        if (categoryHighId == null) return b;
        return b.filter(f -> f
                .term(t -> t
                        .field("category_high_id")
                        .value(categoryHighId)
                )
        );
    }


    private BoolQuery.Builder filterUser(BoolQuery.Builder b, Long userId) {
        if (userId == null) return b;
        return b.filter(f -> f
                .term(t -> t
                        .field("user_id")
                        .value(userId)
                )
        );
    }
}