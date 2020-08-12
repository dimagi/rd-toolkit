package org.rdtoolkit.model.session

import org.rdtoolkit.model.session.TestSession
import org.rdtoolkit.model.session.TestSessionDao

class SessionRepository(var testSessionDao : TestSessionDao) {
    fun insert(testSession: TestSession) {
        testSessionDao.save(testSession)
    }

    fun load(sessionId : String) : TestSession {
        return testSessionDao.load(sessionId)
    }
}