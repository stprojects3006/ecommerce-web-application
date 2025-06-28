import Head from "next/head";
import Link from "next/link";

export default function Page2() {
  return (
    <>
      <Head>
        <title>Page 2</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main>
        <h1>Page 2</h1>
        <ul>
          <li>
            <Link href="/">Home</Link>
          </li>
        </ul>
      </main>
    </>
  );
}
