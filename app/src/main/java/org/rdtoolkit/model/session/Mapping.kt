package org.rdtoolkit.model.session

import org.rdtoolkit.support.model.Mapper
import org.rdtoolkit.support.model.session.TestSession

class SessionToDataMapper() : Mapper<TestSession, DataTestSession> {
    override fun map(input: TestSession): DataTestSession {
        return DataTestSession(
                DbTestSession(input.sessionId, input.state, input.testProfileId,
                        input.timeStarted, input.timeResolved, input.timeExpired),
                ConfigToDataMapper(input.sessionId).map(input.configuration),
                ResultToDataMapper(input.sessionId).map(input.result)
        )
    }
}

class DataToSessionMapper() : Mapper<DataTestSession, TestSession> {
    override fun map(input: DataTestSession): TestSession {
        val session = input.session
        return TestSession(session.sessionId, session.state, session.testProfileId,
                DataToConfigMapper().map(input.config),
                session.timeStarted,
                session.timeResolved,
                session.timeExpired,
                DataToResultMapper().map(input.result))
    }
}

class ResultToDataMapper(val sessionId: String) : Mapper<TestSession.TestResult?, DbTestSessionResult?> {
    override fun map(input: TestSession.TestResult?): DbTestSessionResult? {
        return input?.let{ DbTestSessionResult(sessionId, input.timeRead, input.mainImage, input.images, input.results, input.classifierResults) }
    }
}

class DataToResultMapper() : Mapper<DbTestSessionResult?, TestSession.TestResult?> {
    override fun map(input: DbTestSessionResult?): TestSession.TestResult? {
        return input?.let{ TestSession.TestResult(input.timeRead, input.mainImage, input.images, input.results, input.classifierResults) }
    }
}

class ConfigToDataMapper(val sessionId : String) : Mapper<TestSession.Configuration, DbTestSessionConfiguration> {
    override fun map(input: TestSession.Configuration): DbTestSessionConfiguration {
        return DbTestSessionConfiguration(sessionId, input.sessionType, input.provisionMode, input.classifierMode, input.provisionModeData, input.flavorText, input.flavorTextTwo, input.outputSessionTranslatorId, input.outputResultTranslatorId, input.cloudworksDns, input.cloudworksContext, input.flags)
    }
}

class DataToConfigMapper() : Mapper<DbTestSessionConfiguration, TestSession.Configuration> {
    override fun map(input : DbTestSessionConfiguration): TestSession.Configuration {
        return TestSession.Configuration(input.sessionType, input.provisionMode, input.classifierMode, input.provisionModeData, input.flavorText, input.flavorTextTwo, input.outputSessionTranslatorId, input.outputResultTranslatorId, input.cloudworksDns, input.cloudworksContext, input.flags)
    }
}