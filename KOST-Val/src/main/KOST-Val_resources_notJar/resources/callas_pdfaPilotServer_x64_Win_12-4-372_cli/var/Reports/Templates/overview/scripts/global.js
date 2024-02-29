function isArrayEqual(a, b) {
    return Array.isArray(a) &&
        Array.isArray(b) &&
        a.length === b.length &&
        (function(){
            for(let i = 0; i < a.length; i++) {
                if(a[i] !== b[i]) {
                    return false;
                }
            }
            return true;
        })();
}

function isObjectEqual(a, b) {
    let aProps = Object.getOwnPropertyNames(a);
    let bProps = Object.getOwnPropertyNames(b);

    if(aProps.length != bProps.length) {
        return false;
    }

    for(let i = 0; i < aProps.length; i++) {
        let propName = aProps[i];
        let isArray = Array.isArray(a[propName]);

        if(isArray) {
            if(!isArrayEqual(a[propName], b[propName])) {
                return false;
            }
        }else {
            if (a[propName] !== b[propName]) {
                return false;
            }
        }
    }

    return true;
}