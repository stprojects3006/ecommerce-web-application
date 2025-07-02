import './logo.css'
import {Link} from 'react-router-dom'
import logoSvg from '../../assets/images/icons/logo.svg'

function Logo() {
    return (
        <Link to='/'>
            <img src={logoSvg} alt="AFLOW Logo" style={{height: '80px', display: 'block'}} />
        </Link>
    );
}

export default Logo;