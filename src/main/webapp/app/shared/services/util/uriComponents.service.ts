export const serializeUriComponents = function(obj) {
  const str = [];
  for (const p in obj) {
    if (obj.hasOwnProperty(p) && obj[p] !== undefined) {
      if (obj[p] instanceof Array) {
        for (let i = 0; i < obj[p].length; ++i) {
          str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p][i]));
        }
      } else {
        str.push(encodeURIComponent(p) + '=' + encodeURIComponent(obj[p]));
      }
    }
  }
  return str.join('&');
};

/*
 * Parse URI components to js data types, since this is not supported by js by default.
 *
 * takes in a query string, and returns an object with keys and values with the (hopefully) correct data types
 * */
export const parseUriComponents = uri => {
  let vals = null;
  if (typeof uri === 'string') {
    vals = uri.split('&');
    return parseUriStrings(vals);
  } else if (typeof uri === 'object' && !Array.isArray(uri)) {
    return parseUriObject(uri);
  } else if (typeof uri === 'object' && Array.isArray(uri)) {
    return parseUriStrings(uri);
  } else {
    console.error('cannot parse object to uri components', uri);
    return null;
  }
};

const parseUriObject = obj => {
  obj = Object.assign({}, obj);
  for (const val in obj) {
    if (!obj.hasOwnProperty(val)) {
      continue;
    }

    if (Array.isArray(obj[val])) {
      const arr = [];
      for (const item of obj[val]) {
        arr.push(guessDataType(decodeURIComponent(item)));
      }
      obj[val] = arr;
    } else if (typeof val === 'object') {
      obj[val] = parseUriObject(obj[val]);
    } else {
      // simple value
      obj[val] = guessDataType(decodeURIComponent(obj[val]));
    }
  }
  return obj;
};

const parseUriStrings = arr => {
  const obj = {};
  for (const item of arr) {
    const [key, val] = item.split('=');
    const decKey = decodeURIComponent(key);
    const decVal = guessDataType(decodeURIComponent(val));
    if (obj[decKey]) {
      if (!Array.isArray(obj[decKey])) {
        obj[decKey] = [decVal];
      }
      obj[decKey].push(decVal);
    } else {
      obj[decKey] = decVal;
    }
  }
};

const guessDataType = val => {
  if (val === 'true' || val === 'false') {
    return val === 'true';
  }
  if (val.match(/^[0-9(.|,){1}]+$/)) {
    return parseFloat(val);
  }
  // check for date -> not necessary for gardle uses, as vuetify uses datestrings for datepickers
  // const date = new Date(val);
  // if (date instanceof Date && !isNaN(date.getTime())) {
  //   return date;
  // }
  // if not boolean, number or Date return string
  return val;
};
