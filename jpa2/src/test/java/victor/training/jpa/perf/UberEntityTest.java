package victor.training.jpa.perf;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UberEntityTest {
    private static final Logger log = LoggerFactory.getLogger(UberEntityTest.class);

    @Autowired
    private EntityManager em;

    private final Country romania = new Country(1L, "Romania");
    private final User testUser = new User(1L,"test");
    private final Scope globalScope = new Scope(1L,"Global");

    @Test
    public void greedyQuery() {
        em.persist(romania);
        em.persist(testUser);
        em.persist(globalScope);


        UberEntity uber = new UberEntity()
                .setName("Zmart Name")
                .setFiscalCountry(romania)
                .setOriginCountry(romania)
                .setInvoicingCountry(romania)
                .setCreatedBy(testUser)
                .setNationality(romania)
                .setScope(globalScope);
        em.persist(uber);

        TestTransaction.end();
        TestTransaction.start();

        log.info("Now, loading by id...");
        log.info("Now, loading again by id...");
        LightUber light = em.createQuery(
                "SELECT new victor.training.jpa.perf.LightUber" +
                        "(u.id, u.name, u.originCountry.id, COUNT(c.id)) " +
                        " FROM UberEntity u LEFT JOIN u.children c WHERE u.id = :id"
        , LightUber.class)
                .setParameter("id", uber.getId())
                .getSingleResult();
        log.info("Loaded");
//        log.info(uberEntity.getOriginCountry().getName());
        log.info("After");
        // TODO fetch only the necessary data
        // TODO change link types?
        log.info("The entity is: id={}, name={}, countryId={}, count={}",
                light.getId(),
                light.getName(),
                light.getOriginCountryId(),
                light.getChildrenCount()
        );
    }
}


class LightUber {
    private final Long id;
    private final String name;
    private final Long originCountryId;
    private final Long childrenCount;

    public LightUber(Long id, String name, Long originCountryId, Long childrenCount) {
        this.id = id;
        this.name = name;
        this.originCountryId = originCountryId;
        this.childrenCount = childrenCount;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getOriginCountryId() {
        return originCountryId;
    }

    public Long getChildrenCount() {
        return childrenCount;
    }
}
