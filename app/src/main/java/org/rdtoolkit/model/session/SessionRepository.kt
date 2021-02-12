package org.rdtoolkit.model.session

import org.rdtoolkit.support.model.session.TestSession
import org.rdtoolkit.support.model.session.TestSessionTraceEvent

interface SessionRepository {
    fun write(testSession: TestSession)

    fun exists(sessionId : String) : Boolean

    fun getTestSession(sessionId : String) : TestSession

    fun loadSessions(): List<TestSession>

    fun clearSession(sessionId: String) : Boolean

    fun recordTraceEvent(event: TestSessionTraceEvent)

    fun loadTraceEvents(sessionId : String) : List<TestSessionTraceEvent>

    fun clearTraceEvents(sessionId : String)
}