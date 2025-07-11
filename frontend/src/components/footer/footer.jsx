import CopyRight from './copyright';
import './footer.css'

function Footer() {
    

    return (
        <>
            <footer>
                <div>
                    <h1>Newsletter</h1>
                    <p>Stay tuned with us!</p>
                    <input type='text' placeholder='Enter your email'/>
                    <button>Submit</button>
                </div>

                <div>
                    <h1>Your Journey To Wellness</h1>
                    <p>We aim to partner with our customers on each of their unique wellness journeys, which is why the team at Wellness Warehouse works tirelessly to keep you living life well.</p>
                    <button>Learn more</button>
                </div>


                <div>
                    <h1>Contact Us</h1>
                    <div>
                        <p><span><i className="fa fa-map-marker"></i></span>Pimpri-Chinchwad, Maharashtra, India</p>
                        <p><span><i className="fa fa-phone"></i></span>+27 82 123 4567</p>
                        <p><span><i className="fa fa-envelope"></i></span>support@affluenceit.com</p>
                    </div>
                    <p>
                        <span><i className="fa fa-facebook"></i></span>
                        <span><i className="fa fa-twitter"></i></span>
                        <span><i className="fa fa-instagram"></i></span>
                        <span><i className="fa fa-youtube"></i></span>
                    </p>
                </div>
            </footer>
            <CopyRight />
        </>
    )
}

export default Footer;