import Head from "next/head";
import Link from "next/link";
import { GetServerSideProps } from "next";
import bodyParser from "body-parser";
import { promisify } from "util";

const getBody = promisify(bodyParser.urlencoded({ extended: true }));

type PageProps = {
    post_data: string;
};

export default function Page3({ pageProps }: { pageProps: PageProps }) {
    const my_txt = pageProps?.post_data;
    return (
        <>
            <Head>
                <title>Page 3 - POST body check</title>
                <link rel="icon" href="/favicon.ico" />
            </Head>

            <main>
                <h1>Page 3 - POST body check</h1>
                <p>
                    This page allows you to post some content to itself to verify the Queue-it triggered by a POST
                    request.
                </p>
                <pre>Post data: {my_txt}</pre>
                <form method="POST">
                    <textarea
                        name="my_text_area"
                        defaultValue={my_txt}
                        placeholder="put your content here"
                        cols={80}
                        rows={5}
                    ></textarea>
                    <br />
                    <button type="submit">Submit</button>
                </form>
                <br />
                <br />
                <Link href="/">Home</Link>
            </main>
        </>
    );
}

export const getServerSideProps: GetServerSideProps = async (ctx: any): Promise<any> => {
    const req = ctx.req;
    const res = ctx.res;

    let post_data = "";
    if (req.method == "POST") {
        await getBody(req, res);
        post_data = req.body.my_text_area;
    }

    return {
        props: { pageProps: { post_data: post_data } },
    };
};
