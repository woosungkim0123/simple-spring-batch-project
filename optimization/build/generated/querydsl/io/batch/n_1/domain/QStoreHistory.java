package io.batch.n_1.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QStoreHistory is a Querydsl query type for StoreHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStoreHistory extends EntityPathBase<StoreHistory> {

    private static final long serialVersionUID = 1091001720L;

    public static final QStoreHistory storeHistory = new QStoreHistory("storeHistory");

    public final StringPath employeeNames = createString("employeeNames");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath productNames = createString("productNames");

    public final StringPath storeName = createString("storeName");

    public QStoreHistory(String variable) {
        super(StoreHistory.class, forVariable(variable));
    }

    public QStoreHistory(Path<? extends StoreHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QStoreHistory(PathMetadata metadata) {
        super(StoreHistory.class, metadata);
    }

}

