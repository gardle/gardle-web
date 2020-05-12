import { serializeUriComponents } from '@/shared/services/util/uriComponents.service';

describe('uriComponents service', () => {
  it('should serialize normal object correctly', () => {
    const actual = serializeUriComponents({
      test: 'string',
      num: 5,
      bool: true
    });

    expect(actual).toBe('test=string&num=5&bool=true');
  });

  it('should serialize object with array correctly', () => {
    const actual = serializeUriComponents({
      test: 'string',
      num: 5,
      arr: ['test', 'of', 'array', 'encoding']
    });

    expect(actual).toBe('test=string&num=5&arr=test&arr=of&arr=array&arr=encoding');
  });
});
