import Head from "next/head";
import Link from "next/link";

export default function Page1() {
  return (
    <>
      <Head>
        <title>Page 1</title>
        <link rel="icon" href="/favicon.ico" />
      </Head>

      <main>
        <h1>Page 1</h1>
        <ul>
          <li>
            <Link href="/">Home</Link>
          </li>
        </ul>
      </main>
    </>
  );
}
