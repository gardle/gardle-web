import { escapeData, escapeHtml } from '@/shared/utils';

describe('utils', () => {
  it('should escape HTML string only once', () => {
    const str = 'This is an <script>alert("injections")</script> &#732;';
    const escapedStr = escapeHtml(str);

    expect(escapedStr).toEqual('This is an &lt;script&gt;alert(&quot;injections&quot;)&lt;/script&gt; &amp;#732;');
    expect(escapeHtml(escapedStr)).toEqual(escapedStr);
  });

  it('should escape a more complex object correctly', () => {
    const obj = {
      test: 'String with <script>alert("error")</script>',
      nested: {
        test: '&#712; hello && some ra&#332;ndom text &#039;'
      },
      arr: ['test ra&#332;ndom text &#039;'],
      arrObj: [
        {
          test: 'very &#921; deeply <script>inject()</script> nested &&'
        }
      ]
    };

    const escapedObj = {
      test: 'String with &lt;script&gt;alert(&quot;error&quot;)&lt;/script&gt;',
      nested: {
        test: '&amp;#712; hello &amp;&amp; some ra&amp;#332;ndom text &#039;'
      },
      arr: ['test ra&amp;#332;ndom text &#039;'],
      arrObj: [
        {
          test: 'very &amp;#921; deeply &lt;script&gt;inject()&lt;/script&gt; nested &amp;&amp;'
        }
      ]
    };

    expect(escapeData(obj)).toEqual(escapedObj);
    expect(escapeData(escapeData(obj))).toEqual(escapedObj);
  });
});
