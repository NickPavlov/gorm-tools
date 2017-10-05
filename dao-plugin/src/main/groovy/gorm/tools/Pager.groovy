package gorm.tools

import gorm.tools.beans.BeanPathTools
import grails.gorm.PagedResultList
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.datastore.mapping.query.Query
import org.hibernate.criterion.Projections
import org.hibernate.internal.CriteriaImpl

/**
 * a holder object for paged data
 */
@CompileStatic
class Pager {
    private Log log = LogFactory.getLog(getClass())
    //the page we are on
    Integer page = 1
    //max rows to show
    Integer max = 10
    //the max rows the user can set it to
    Integer allowedMax = 10000
    //the total record count. This is used to calculate the number of pages
    Integer recordCount = 0
    Integer offset
    List data
    Map params

    public Pager() {}

    public Pager(Map params) {
        setParams(params)
    }

    static Integer max(Map p, Integer defaultMax = 100) {
        Integer defmin = p.max ? toInteger(p.max) : 10
        p.max = Math.min(defmin, defaultMax)
        return p.max as Integer
    }

    static Integer page(Map p) {
        p.page = p.page ? toInteger(p.page) : 1
        return p.page as Integer
    }

    def setParams(Map params) {
        page = params.page = params.page ? toInteger(params.page) : 1
        max = params.max = Math.min(params.max ? toInteger(params.max) : 10, allowedMax)
        this.params = params
    }

    @CompileDynamic
    static Integer toInteger(Object v){
        return v.toInteger()
    }

    Integer getOffset() {
        if (!offset) {
            return (max * (page - 1))
        } else {
            return offset
        }
    }

    Integer getPageCount() {
        return Math.ceil(recordCount / max).intValue()
    }

    def eachPage(Closure c) {
        if(pageCount < 1) return
        log.debug "eachPage total pages : pageCount"

        (1..pageCount).each {Integer pageNum ->
            page = pageNum
            offset = (max * (page - 1))
            try {
                log.debug "Executing eachPage closer with [max:$max, offset:$offset]"
                c.call(max, offset)
            }catch (Exception e) {
                log.error "Error encountered while calling closure in eachPage [max:$max, offset:$offset]}]", e
                throw e
            }
        }

    }

    Map getJsonData() {
        //page is the page we are on, total is the total number f pages based on max per page setting
        //records is the total # of records we have
        //rows are the data
        return [
            page: this.page,
            total: this.getPageCount(),
            records: this.recordCount,
            rows: data
        ]
    }

    Pager setupData(List dlist, List fieldList = null) {
        setData(dlist)
        if (dlist?.size() > 0) {
            if (dlist.hasProperty('totalCount')) {
                setRecordCount(dlist.getProperties().totalCount as Integer)
            } else if (dlist instanceof PagedResultList) {
                setRecordCount(loadTotalFromDb(dlist))
            } else {
                log.warn("Cannot get totalCount for ${dlist.class}")
                setRecordCount(dlist.size())
            }
        }

        if (fieldList) {
            this.data = dlist.collect { obj ->
                return BeanPathTools.buildMapFromPaths(obj, fieldList, true)
            }
        }
        return this
    }

    @CompileDynamic
    int loadTotalFromDb(PagedResultList src) {
        int pageSize = src.size() //we _NEED_ this call, to make sure that data is already fetched,
        //before we changed original query

        if (pageSize == 0) { //actually this is just to double check that src.size() was actually called, result is not so important
            log.warn("Page size is zero, but we will try to load count(*) anyway")
        }

        //get original query and modify it to get count of records. sadly, we cannot clone it
        Query q = src.query
        def criteriaField = q.class.declaredFields.find { it.name == 'criteria' }
        criteriaField.setAccessible(true)
        CriteriaImpl realCriteria = criteriaField.get(q)
        realCriteria.setProjection(Projections.rowCount()) // count(*)

        //now we need to remove ORDER BY, because MS SQL cannot execute count(*) query with ORDER BY
        def currentOrder = realCriteria.class.declaredFields.find { it.name == 'orderEntries' }
        currentOrder.setAccessible(true)
        currentOrder.get(realCriteria).clear()

        //actual count
        return realCriteria.uniqueResult() as Integer ?: 0
    }

}
