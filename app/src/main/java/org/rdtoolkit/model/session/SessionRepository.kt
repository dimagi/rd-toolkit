package org.rdtoolkit.model.session

import org.rdtoolkit.model.session.DbTestSession
import org.rdtoolkit.model.session.TestSessionDao

class SessionRepository(var testSessionDao : TestSessionDao) {

    fun insert(testSession: TestSession) {
        testSessionDao.save(SessionToDataMapper().map(testSession))
    }

    fun getTestSession(sessionId : String) : TestSession {
        return DataToSessionMapper().map(testSessionDao.loadDataSession(sessionId))
    }
}