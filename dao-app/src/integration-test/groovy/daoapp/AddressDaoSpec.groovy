package daoapp

import grails.test.spock.IntegrationSpec

class AddressDaoSpec extends IntegrationSpec {

    static transactional = false


    void "test update with default dao"() {
        setup:
        Address address = new Address(city: "city1").save(flush:true)
        def dao = Address.dao

        when:
        dao.update([id: address.id, city: "city2"])

        Address updatedAddress = Address.get(address.id)
        updatedAddress.refresh()

        then:
        updatedAddress.city == "city2"

    }

    void "test data rollback in update with default dao"() {
        setup:
        Address address = new Address(city: "city1").save(flush:true)
        def dao = Address.dao

        when:
        dao.update1([id: address.id, city: "city2"])

        then:
        thrown Exception

        Address updatedAddress = Address.get(address.id)
        updatedAddress.refresh()

        then:
        updatedAddress.city == "city1"

    }

}
