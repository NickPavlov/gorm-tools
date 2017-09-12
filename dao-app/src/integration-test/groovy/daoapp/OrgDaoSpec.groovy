package daoapp

import grails.test.spock.IntegrationSpec

class OrgDaoSpec extends IntegrationSpec {

    static transactional = false

    //without @Transactional
    def orgDao

    //with @Transactional
    def orgDelegateDao

    //should fail
    void "test update when OrgDao with @Transactional"() {
        setup:
        Org org = new Org(name: "test1").save()

        when:
        orgDelegateDao.update([id: org.id, name: "test2"])

        Org updatedOrg = Org.get(org.id)
        updatedOrg.refresh()

        then:
        updatedOrg.name == "test2"

    }

    void "test update when OrgDao without @Transactional"() {
        setup:
        Org org = new Org(name: "test1").save()

        when:
        orgDao.update([id: org.id, name: "test2"])

        Org updatedOrg = Org.get(org.id)
        updatedOrg.refresh()

        then:
        updatedOrg.name == "test2"

    }

    void "test overridden update() when OrgDao with @Transactional"() {
        setup:
        Org org = new Org(name: "test1").save()

        when:
        orgDelegateDao.updateTest([id: org.id, name: "test2"])

        Org updatedOrg = Org.get(org.id)
        updatedOrg.refresh()

        then:
        updatedOrg.name == "test2"

    }

    void "test data rollback in update() method with @Transactional"() {
        setup:
        Org org = new Org(name: "test1").save()

        when:
        orgDelegateDao.updateWithException([id: org.id, name: "test2"])

        then:
        thrown RuntimeException

        when:
        Org updatedOrg = Org.get(org.id)
        updatedOrg.refresh()

        then:

        updatedOrg.name == "test1"

    }
}
