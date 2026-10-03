import { Link } from 'react-router-dom'

// MIT requires the copyright + permission notice to travel with substantial portions of
// the licensed work - most of the palettes derive from this dataset, so it's reproduced
// in full below rather than just linked.
const MATTDESL_MIT_LICENSE = `MIT License

Copyright (c) 2020 Matt DesLauriers

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.`

const linkClass = 'underline underline-offset-2 hover:text-stone-900 transition-colors'

function ExternalLink({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <a href={href} target="_blank" rel="noopener noreferrer" className={linkClass}>
      {children}
    </a>
  )
}

export function AboutPage() {
  return (
    <div className="max-w-3xl mx-auto px-6 py-8">
      <Link
        to="/"
        className="text-sm text-stone-600 hover:text-stone-700 transition-colors mb-8 inline-flex items-center gap-1"
      >
        ← All palettes
      </Link>

      <h1 className="text-3xl font-semibold text-stone-900 leading-tight mt-6">About the colors</h1>

      <div className="text-stone-700 leading-relaxed space-y-4 mt-6">
        <p>
          The palettes in Kasane come from Sanzo Wada's <em>A Dictionary of Color Combinations</em>,
          a six-volume study of color pairings first published in Japan in 1933.
        </p>
      </div>

      <section className="border border-stone-200 rounded-2xl bg-white p-6 mt-8">
        <h2 className="text-lg font-semibold text-stone-900 mb-4">Sources &amp; attribution</h2>
        <dl className="space-y-5 text-sm text-stone-700 leading-relaxed">
          <div>
            <dt className="font-medium text-stone-900">Color &amp; palette data</dt>
            <dd className="mt-1">
              Data from <ExternalLink href="https://colorcombinations.org/data/">colorcombinations.org</ExternalLink>,
              licensed under{' '}
              <ExternalLink href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</ExternalLink>.
              The data has been adapted for Kasane: it is loaded into Kasane's own database and
              presented, filtered, and searched through this app's interface.
            </dd>
          </div>

          <div>
            <dt className="font-medium text-stone-900">Original palette dataset</dt>
            <dd className="mt-1">
              348 of the 378 palettes were originally compiled in{' '}
              <ExternalLink href="https://github.com/mattdesl/dictionary-of-colour-combinations">
                mattdesl/dictionary-of-colour-combinations
              </ExternalLink>
              , © 2020 Matt DesLauriers, released under the MIT License.
              <details className="mt-2">
                <summary className="cursor-pointer text-stone-600 hover:text-stone-900 transition-colors">
                  Show license text
                </summary>
                <pre className="mt-2 p-4 bg-stone-50 border border-stone-100 rounded-lg text-xs text-stone-600 whitespace-pre-wrap font-mono">
                  {MATTDESL_MIT_LICENSE}
                </pre>
              </details>
            </dd>
          </div>

          <div>
            <dt className="font-medium text-stone-900">Original work</dt>
            <dd className="mt-1">
              Sanzo Wada, <em>A Dictionary of Color Combinations</em>, 1933 (six volumes).
              Modern reprint: Seigensha Art Publishing, 2010.
            </dd>
          </div>
        </dl>
      </section>

      <p className="text-xs text-stone-600 mt-6">
        The attributions above cover the color data only. Kasane's own source code is separately
        licensed under the MIT License.
      </p>
    </div>
  )
}
