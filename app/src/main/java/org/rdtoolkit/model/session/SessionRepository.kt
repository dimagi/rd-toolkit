package org.rdtoolkit.model.session

import org.rdtoolkit.support.model.ListMapperImpl
import org.rdtoolkit.support.model.session.TestSession

interface SessionRepository {
    fun write(testSession: TestSession)

    fun getTestSession(sessionId : String) : TestSession

    fun loadSessions(): List<TestSession>

    fun clearSession(sessionId: String) : Boolean
}