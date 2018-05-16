// a reusable script is a standalone script - a script that is not embedded in a configuration file.
// these kind of scripts are reusable and easier to manage as they support better syntax highlighting.

// declared variables are only scoped to the current execution
// to persist state across script invocations - use state variables
// on the context object.
var a = 5;
var b = 5;

// the last expression is the returned value - as we are not executed within an actual function.
a == b;