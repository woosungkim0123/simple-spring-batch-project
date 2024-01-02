package optimization.reader.options;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.annotation.Nonnull;
import optimization.reader.expression.Expression;

public class QuerydslNoOffsetNumberOptions<T, N extends Number & Comparable<?>> extends QuerydslNoOffsetOptions<T> {

    private N currentId;
    private N lastId;

    private final NumberPath<N> field;

    public QuerydslNoOffsetNumberOptions(@Nonnull NumberPath<N> field,
                                         @Nonnull Expression expression) {
        super(field, expression);
        this.field = field;
    }

    public N getCurrentId() {
        return currentId;
    }

    public N getLastId() {
        return lastId;
    }

    @Override
    public void initKeys(JPAQuery<T> query, int page) {
        if(page == 0) {
            initFirstId(query);
            initLastId(query);
        }
    }

    /**
     * 만들어지는 쿼리 예시
     * select min(product.id)
     * from Product product
     * where product.createDate = ?1
     */
    @Override
    protected void initFirstId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();
        boolean isGroupByQuery = isGroupByQuery(clone);

        if(isGroupByQuery) {
            currentId = clone
                    .select(field)
                    .orderBy(expression.isAsc()? field.asc() : field.desc())
                    .fetchFirst();
        } else {
            currentId = clone
                    .select(expression.isAsc()? field.min(): field.max())
                    .fetchFirst();
        }
    }

    /**
     * 만들어지는 쿼리 예시
     * select max(product.id)
     * from Product product
     * where product.createDate = ?1
     */
    @Override
    protected void initLastId(JPAQuery<T> query) {
        JPAQuery<T> clone = query.clone();
        boolean isGroupByQuery = isGroupByQuery(clone);

        if(isGroupByQuery) {
            lastId = clone
                    .select(field)
                    .orderBy(expression.isAsc()? field.desc() : field.asc())
                    .fetchFirst();
        } else {
            lastId = clone
                    .select(expression.isAsc()? field.max(): field.min())
                    .fetchFirst();
        }
    }

    /**
     * select product
     * from Product product
     * where product.createDate = ?1 and (product.id >= ?2 and product.id <= ?3)
     * order by product.id asc
     */
    @Override
    public JPAQuery<T> createQuery(JPAQuery<T> query, int page) {
        if(currentId == null) {
            return query;
        }
        // whereExpression(page) => product.id >= 1 && product.id <= 1000000
        return query
                .where(whereExpression(page))
                .orderBy(orderExpression());
    }

    private BooleanExpression whereExpression(int page) {
        return expression.where(field, page, currentId)
                .and(expression.isAsc()? field.loe(lastId) : field.goe(lastId));
    }

    private OrderSpecifier<N> orderExpression() {
        return expression.order(field);
    }

    @Override
    public void resetCurrentId(T item) {
        currentId = (N) getFiledValue(item);
    }
}
