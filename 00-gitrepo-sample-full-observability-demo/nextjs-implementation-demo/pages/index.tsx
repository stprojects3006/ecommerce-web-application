import Head from "next/head";
import Link from "next/link";

export default function Index() {
  return (
    <>
      <Head>
        <title>Home</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main>
        <h1>Home</h1>
        <ul>
          <li>
            <Link href="/page1">Page 1</Link>
          </li>
          <li>
            <Link href="/page2">Page 2</Link>
          </li>
          <li>
            <Link href="/page3">Page 3 (POST body check)</Link>
          </li>
        </ul>
      </main>
    </>
  );
}
