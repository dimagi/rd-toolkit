package org.rdtoolkit.model.session

import androidx.lifecycle.LiveData
import org.rdtoolkit.model.ListMapperImpl
import org.rdtoolkit.model.session.DbTestSession
import org.rdtoolkit.model.session.TestSessionDao

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