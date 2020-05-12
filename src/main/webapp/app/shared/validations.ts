import { helpers } from 'vuelidate/lib/validators';
import { compareAsc, parseISO, subYears } from 'date-fns';

const usernameRegex = new RegExp(/^[_.@A-Za-z0-9-]*$/);

export const isOlderThan = amount =>
  helpers.withParams(
    { type: 'isOlderThan', minYears: amount },
    birthdate => birthdate !== null && compareAsc(subYears(new Date(), amount), parseISO(birthdate)) >= 0
  );
export const acceptance = val => val !== null && val === true;
export const username = name => name !== null && usernameRegex.test(name);

// validates array that contains minimum number at index 0 and maximum at index 1, needed for e.g. sliders
export const validMinMaxPositiveNumericParameterArray = minMaxParamArray => {
  if (minMaxParamArray && minMaxParamArray.length === 2) {
    return minMaxParamArray[0] >= 0 && minMaxParamArray[1] >= 0 && minMaxParamArray[0] <= minMaxParamArray[1];
  } else {
    return false;
  }
};
