package org.rdtoolkit.model.session

import org.rdtoolkit.processing.WorkCoordinator
import org.rdtoolkit.support.model.ListMapperImpl
import org.rdtoolkit.support.model.session.STATUS
import org.rdtoolkit.support.model.session.TestSession

class SessionRepositoryImpl(private var testSessionDao : TestSessionDao,
                            private val workCoordinator : WorkCoordinator) : SessionRepository{
    override fun write(testSession: TestSession) {
        testSessionDao.save(SessionToDataMapper().map(testSession))

        if(testSession.state == STATUS.COMPLETE) {
            testSession.state = STATUS.QUEUED
            workCoordinator.processTestSession(testSession)
            testSessionDao.save(SessionToDataMapper().map(testSession))
        }
    }

    override fun getTestSession(sessionId : String) : TestSession {
        return DataToSessionMapper().map(testSessionDao.loadDataSession(sessionId))
    }

    override fun loadSessions(): List<TestSession> {
        return ListMapperImpl(DataToSessionMapper()).map(testSessionDao.loadSessions())
    }

    override fun clearSession(sessionId: String) : Boolean {
        return testSessionDao.delete(sessionId) > 0
    }

}