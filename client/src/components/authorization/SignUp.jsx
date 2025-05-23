import { useContext, useState } from 'react';
import AuthContext from '../../context/AuthContext';
import useForm from '../../hooks/useForm';
import usePost from '../../hooks/usePost';
import ErrorMessage from '../ErrorMessage';

/**
 * Component with sing up form
 * @param {*} props - expects modeToggle component with button to
 * toggle between sing form up and sing in form
 */
function SignUp({ modeToggle }) {
    const [inputValues, addInput] = useForm();
    const [inputError, setInputError] = useState(null);
    const authorization = useContext(AuthContext);
    const postCredentials = usePost(
        '/user/sign_up',
        (authData) => {
            authorization.authorize(
                authData.username,
                authData.token,
                authData.name,
                authData.surname,
            );
        },
    );

    const onSingUpClick = () => {
        if (inputValues.password !== inputValues.confirmation) {
            setInputError('Пароли не совпадают');
            return;
        }
        if (Object.values(inputValues).some((value) => !value)) {
            setInputError('Заполните все поля');
            return;
        }

        setInputError(null);
        postCredentials.fetch(
            JSON.stringify({
                username: inputValues.username,
                name: inputValues.name,
                surname: inputValues.surname,
                password: inputValues.password,
            }),
            { headers: { 'Content-Type': 'application/json' } },
        );
    };

    return (
        <div>
            <h2> Регистрация </h2>
            <ErrorMessage message={postCredentials.error || inputError} />
            <p> Логин </p>
            <input {...addInput('username')} />
            <p> Имя </p>
            <input {...addInput('name')} />
            <p> Фамилия </p>
            <input {...addInput('surname')} />
            <p> Пароль </p>
            <input {...addInput('password', 'password')} />
            <p> Подтвердите пароль </p>
            <input {...addInput('confirmation', 'password')} />
            <div className="authorization-buttons-panel">
                <button disabled={postCredentials.isLoading} onClick={onSingUpClick}>
                    Зарегистрироваться
                </button>
                {modeToggle}
            </div>
        </div>
    );
}

export default SignUp;
