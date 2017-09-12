package daoapp

import grails.plugin.dao.GormDaoSupport
import grails.transaction.Transactional

@Transactional
class OrgDelegateDao extends GormDaoSupport{
	
	Class domainClass = Org


	Map insert(params){
		def madeNameDefault
		if(!params.name){
			params.name = "default"
		}
		super.insert(params)
	}

	Map updateTest(params) {
		return super.update(params)
	}

	Map updateWithException(params) {
		def result = super.update(params)
		throw new RuntimeException()

		return result
	}
}

