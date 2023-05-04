package PlayMakers.SportsIT.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCompetition is a Querydsl query type for Competition
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCompetition extends EntityPathBase<Competition> {

    private static final long serialVersionUID = -1044497266L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCompetition competition = new QCompetition("competition");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final ListPath<CompetitionAgree, QCompetitionAgree> agreements = this.<CompetitionAgree, QCompetitionAgree>createList("agreements", CompetitionAgree.class, QCompetitionAgree.class, PathInits.DIRECT2);

    public final EnumPath<SportCategory> category = createEnum("category", SportCategory.class);

    public final NumberPath<Long> competitionId = createNumber("competitionId", Long.class);

    public final EnumPath<CompetitionType> competitionType = createEnum("competitionType", CompetitionType.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final QMember host;

    public final StringPath location = createString("location");

    public final StringPath locationDetail = createString("locationDetail");

    public final NumberPath<Integer> maxPlayer = createNumber("maxPlayer", Integer.class);

    public final NumberPath<Integer> maxViewer = createNumber("maxViewer", Integer.class);

    public final StringPath name = createString("name");

    public final ListPath<Poster, QPoster> posters = this.<Poster, QPoster>createList("posters", Poster.class, QPoster.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> recruitingEnd = createDateTime("recruitingEnd", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> recruitingStart = createDateTime("recruitingStart", java.time.LocalDateTime.class);

    public final NumberPath<Integer> scrapCount = createNumber("scrapCount", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startDate = createDateTime("startDate", java.time.LocalDateTime.class);

    public final EnumPath<CompetitionState> state = createEnum("state", CompetitionState.class);

    public final StringPath stateDetail = createString("stateDetail");

    public final StringPath templateID = createString("templateID");

    public final NumberPath<Integer> totalPrize = createNumber("totalPrize", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QCompetition(String variable) {
        this(Competition.class, forVariable(variable), INITS);
    }

    public QCompetition(Path<? extends Competition> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCompetition(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCompetition(PathMetadata metadata, PathInits inits) {
        this(Competition.class, metadata, inits);
    }

    public QCompetition(Class<? extends Competition> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.host = inits.isInitialized("host") ? new QMember(forProperty("host")) : null;
    }

}
