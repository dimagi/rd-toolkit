package org.rdtoolkit

import org.rdtoolkit.model.session.*
import java.util.*
import kotlin.collections.HashMap


object TestObjects {
    @JvmField
    val TestResultNoValues = TestSession.TestResult(Date(), "testpath", HashMap(), HashMap())

    @JvmField
    val TestResultsSampleValues = TestSession.TestResult(Date(Date().time - 500), "testpath", HashMap(mapOf("diag_one" to "positive", "diag_two" to "negative")), HashMap())

    @JvmField
    val ConfigMinimal = TestSession.Configuration(SessionMode.ONE_PHASE,ProvisionMode.TEST_PROFILE,ClassifierMode.PRE_POPULATE,"test_id",null,null,null,null,HashMap())

    @JvmField
    val ConfigNormal = TestSession.Configuration(SessionMode.ONE_PHASE,ProvisionMode.TEST_PROFILE,ClassifierMode.PRE_POPULATE,"test_id","flavor_one","flavor_two",null,null,HashMap())

    @JvmField
    val SessionProvisioned = TestSession("unique_id",
            STATUS.RUNNING,
            "test_id",
            ConfigNormal,
            Date(Date().time - 1000),
            Date(Date().time - 750),
            Date(Date().time - 250),
            null)


    @JvmField
    val SessionCompleted = TestSession("unique_id",
            STATUS.COMPLETE,
            "test_id",
            ConfigNormal,
            Date(Date().time - 1000),
            Date(Date().time - 750),
            Date(Date().time - 250),
            TestResultsSampleValues)


}