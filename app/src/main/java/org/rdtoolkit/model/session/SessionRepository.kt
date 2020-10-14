package org.rdtoolkit.model.session

import org.rdtoolkit.support.model.ListMapperImpl
import org.rdtoolkit.support.model.session.TestSession

class SessionRepository(var testSessionDao : TestSessionDao) {

    fun write(testSession: TestSession) {
        testSessionDao.save(SessionToDataMapper().map(testSession))
    }

    fun getTestSession(sessionId : String) : TestSession {
        return DataToSessionMapper().map(testSessionDao.loadDataSession(sessionId))
    }

    fun loadSessions(): List<TestSession> {
        return ListMapperImpl(DataToSessionMapper()).map(testSessionDao.loadSessions())
    }
}