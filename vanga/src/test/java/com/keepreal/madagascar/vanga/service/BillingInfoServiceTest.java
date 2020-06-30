package com.keepreal.madagascar.vanga.service;

import com.keepreal.madagascar.vanga.VangaApplication;
import com.keepreal.madagascar.vanga.model.BillingInfo;
import com.keepreal.madagascar.vanga.repository.BillingInfoRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@SpringBootTest(classes = VangaApplication.class)
@RunWith(SpringRunner.class)
public class BillingInfoServiceTest {

    @Autowired
    private BillingInfoService service;
    @Autowired
    private BillingInfoRepository repository;

    @Before
    public void init() {
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setId("test-init");
        billingInfo.setUserId("test-init");
        billingInfo.setCreatedTime(600L);
        repository.save(billingInfo);
    }

    @After
    public void destroy() {
        repository.deleteById("test-init");
    }

    @Test
    public void retrieveOrCreateBillingInfoIfNotExistsByUserId() {
        BillingInfo billingInfo = service.retrieveOrCreateBillingInfoIfNotExistsByUserId("test-init");
        assertEquals(600L, (long) billingInfo.getCreatedTime());

        BillingInfo createBillingInfo = service.retrieveOrCreateBillingInfoIfNotExistsByUserId("test-create");
        assertNotNull(createBillingInfo);
        repository.delete(createBillingInfo);
    }

    @Test
    public void updateBillingInfoByUserId() {
        BillingInfo billingInfoUpdate = service.updateBillingInfoByUserId(
                "test-update",
                "xxx",
                "13888888888",
                "9999999999",
                "6666688888");

        assertNotNull(billingInfoUpdate.getName());
        assertNotNull(billingInfoUpdate.getMobile());
        assertNotNull(billingInfoUpdate.getAccountNumber());
        assertNotNull(billingInfoUpdate.getIdNumber());
        assertEquals(true, billingInfoUpdate.getVerified());

        BillingInfo billingInfoVerified = service.updateBillingInfoByUserId(
                "test-verified",
                "xxx",
                "13888888888",
                "9999999999",
                null);
        assertNull(billingInfoVerified.getIdNumber());
        assertEquals(false, billingInfoVerified.getVerified());

        repository.delete(billingInfoUpdate);
        repository.delete(billingInfoVerified);
    }

    @Test
    public void createNewBillingInfo() {
        BillingInfo newBillingInfo = service.createNewBillingInfo("test-billing");

        assertNotNull(newBillingInfo);

        repository.delete(newBillingInfo);
    }
}