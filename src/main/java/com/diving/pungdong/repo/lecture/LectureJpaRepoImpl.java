package com.diving.pungdong.repo.lecture;

import com.diving.pungdong.domain.lecture.Lecture;
import com.diving.pungdong.domain.lecture.Organization;
import com.diving.pungdong.dto.lecture.list.search.CostCondition;
import com.diving.pungdong.dto.lecture.list.search.FilterSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

import static com.diving.pungdong.domain.lecture.QLecture.lecture;

public class LectureJpaRepoImpl implements LectureJpaRepoCustom {
    private final JPAQueryFactory queryFactory;

    public LectureJpaRepoImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Lecture> searchListByCondition(FilterSearchCondition condition, Pageable pageable) {
        List<Lecture> content = queryFactory
                .selectFrom(lecture)
                .where(
                        classKindEq(condition.getClassKind()),
                        organizationEq(condition.getOrganization()),
                        levelEq(condition.getLevel()),
                        regionEq(condition.getRegion()),
                        costBetween(condition.getCostCondition()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(lecture)
                .where(
                        classKindEq(condition.getClassKind()),
                        organizationEq(condition.getOrganization()),
                        levelEq(condition.getLevel()),
                        regionEq(condition.getRegion()),
                        costBetween(condition.getCostCondition()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression organizationEq(Organization organization) {
        return organization == null ? null : lecture.organization.eq(organization);
    }

    private BooleanExpression classKindEq(String classKind) {
        return classKind == null ? null : lecture.classKind.eq(classKind);
    }
    private BooleanExpression levelEq(String level) {
        return level == null ? null : lecture.level.eq(level);
    }

    private BooleanExpression regionEq(String region) {
        return region == null ? null : lecture.region.eq(region);
    }

    private BooleanExpression costBetween(CostCondition costCondition) {
        return costCondition == null || costCondition.equals(new CostCondition())
                ? null : lecture.price.between(costCondition.getMin(), costCondition.getMax());
    }
}
