package testing

import grails.plugin.dao.DaoUtil
import grails.plugin.dao.DomainException
import spock.lang.Specification

class BasicTestsForDao extends Specification {

	def dao
	
/*	protected void setUp() {
		super.setUp()
		dao = jumperDao
		assert dao.domainClass == Jumper
		//pop an item in just to have for things below
	}

	protected void tearDown() {
		super.tearDown()
	}*/

	void testSave(){
		println "testSave"
		def dom = new Jumper(name:"testSave")
		try{
			dao.save(dom)
			DaoUtil.flushAndClear()
			def dom2 = Jumper.findByName("testSave")
			assert dom2
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testDelete(){
		println "testDelete"
		def dom = new Jumper(name:"testDelete")
		try{
			dao.save(dom)
			DaoUtil.flushAndClear()
			def dom2 = Jumper.findByName("testDelete")
			dao.delete(dom2)
			def dom3 = Jumper.findByName("testDelete")
			dom3 == null
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testInsert(){
		println "testInsert"
		try{
			def result = dao.insert([name:"testInsert"])
			DaoUtil.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals "testInsert", result.entity.name 
			assertEquals "default.created.message", result.message.code
			def dom2 = Jumper.findByName("testInsert")
			assert dom2.name == "testInsert"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testUpdate(){
		println "testUpdate"
		def dup = new Jumper(name:"testUpdate")
		dup.save()
		DaoUtil.flushAndClear()
		assert Jumper.findByName("testUpdate")
		try{
			def result = dao.update([id:dup.id,name:"testUpdateXXX"])
			DaoUtil.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals "testUpdateXXX", result.entity.name 
			assertEquals dup.id, result.entity.id 
			assertEquals "default.updated.message", result.message.code
			def dom2 = Jumper.findByName("testUpdateXXX")
			assert dom2.name == "testUpdateXXX"
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}
	
	void testRemove(){
		println "testRemove"
		def dup = new Jumper(name:"testRemove")
		dup.save()
		DaoUtil.flushAndClear()
		assert Jumper.findByName("testRemove")
		try{
			def result = dao.remove([id:dup.id])
			DaoUtil.flushAndClear()
			//println result
			assertTrue result.ok
			assertEquals dup.id, result.id
			assertEquals "default.deleted.message", result.message.code
			assertNull Jumper.findByName("testRemove")
		}catch(DomainException e){
			fail "Errors ${e.errors.allErrors[0]}"
		}
	}

}

