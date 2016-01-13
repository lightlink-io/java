function run(params, callback) {
    callback(eval(  // transform JSON to object
        "(" + tester.run(params) + ")"
    ));
}
function runAction(actionName,params, callback) {
    callback(eval(  // transform JSON to object
        "(" + tester.runAction(actionName,params) + ")"
    ));
}

function throwError(message) {
    try {
        throw Error('message');
    } catch (err) {
        var line = err.stack.split(/<program> .<eval>:/)[1].split(")")[0];
        throw new Error("Line:" + line + " of " + _scriptName_ + "\n" + message);
    }
}

function assertRunSuccess(runResult) {
    if (!runResult.success) throw new Error("Execution status" + "; Expected :true found:" + runResult.success);
}

function assertEquals(msg, found, expected) {
    if (expected != found)
        throwError(msg + "; Expected :" + expected + ", found:" + found);
    print("Assertion '" + msg + "' PASSED");
}

function assertTrue(msg, value) {
    if (!value) throw new Error(msg + "; Expected :true, found:" + value);
    print("Assertion '" + msg + "' PASSED");
}

function assertFalse(msg, value) {
    if (value) throw new Error(msg + "; Expected :false, found:" + value);
    print("Assertion '" + msg + "' PASSED");
}

function assertNotNull(msg, value) {
    if (!value) throw new Error(msg + "; Expected not null, found:" + value);
    print("Assertion '" + msg + "' PASSED");
}

function assertNull(msg, value) {
    if (value) throw new Error(msg + "; Expected null, found:" + value);
    print("Assertion '" + msg + "' PASSED");
}
