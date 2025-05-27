import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../api/axiosInstance';

function TeacherCreateQuizPage() {
    const navigate = useNavigate();

    const [quiz, setQuiz] = useState({
        name: '',
        description: '',
        topic: '',
        isActive: true,
        durationMinutes: 30,
        questions: [
            {
                questionSentence: '',
                questionTypeId: 1,
                options: [
                    { text: '', isCorrect: true },
                    { text: '', isCorrect: false },
                    { text: '', isCorrect: false },
                    { text: '', isCorrect: false }
                ],
                points: 1
            }
        ]
    });

    const [errors, setErrors] = useState({
        name: false,
        topic: false,
        questions: []
    });

    const QUESTION_TYPES = [
        { value: 1, label: 'Çoktan Seçmeli' },
        { value: 2, label: 'Açık Uçlu' }
    ];

    const addQuestion = () => {
        setQuiz(prev => ({
            ...prev,
            questions: [
                ...prev.questions,
                {
                    questionSentence: '',
                    questionTypeId: 1,
                    options: [
                        { text: '', isCorrect: false },
                        { text: '', isCorrect: false },
                        { text: '', isCorrect: false },
                        { text: '', isCorrect: false }
                    ],
                    points: 1
                }
            ]
        }));
    };

    const removeQuestion = (index) => {
        setQuiz(prev => ({
            ...prev,
            questions: prev.questions.filter((_, i) => i !== index)
        }));
        setErrors(prev => ({
            ...prev,
            questions: prev.questions.filter((_, i) => i !== index)
        }));
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setQuiz(prev => ({ ...prev, [name]: value }));
    };

    const handleQuestionChange = (index, field, value) => {
        setQuiz(prev => ({
            ...prev,
            questions: prev.questions.map((q, i) => {
                if (i === index) {
                    // If changing question type
                    if (field === 'questionTypeId') {
                        return {
                            ...q,
                            questionTypeId: value,
                            // If changing to multiple-choice and options are empty, initialize default options
                            options: value === 1 && (!q.options || q.options.length === 0) ? [
                                { text: '', isCorrect: false },
                                { text: '', isCorrect: false },
                                { text: '', isCorrect: false },
                                { text: '', isCorrect: false }
                            ] : q.options
                        };
                    }
                    // For other fields
                    return {
                        ...q,
                        [field]: value
                    };
                }
                return q;
            })
        }));
    };

    const handleOptionChange = (questionIndex, optionIndex, field, value) => {
        setQuiz(prev => ({
            ...prev,
            questions: prev.questions.map((q, i) =>
                i === questionIndex
                    ? {
                        ...q,
                        options: q.options.map((opt, j) =>
                            j === optionIndex ? { ...opt, [field]: value } : opt
                        )
                    }
                    : q
            )
        }));
    };

    const setCorrectAnswer = (questionIndex, optionIndex) => {
        setQuiz(prev => ({
            ...prev,
            questions: prev.questions.map((q, i) =>
                i === questionIndex
                    ? {
                        ...q,
                        options: q.options.map((opt, j) => ({
                            ...opt,
                            isCorrect: j === optionIndex
                        }))
                    }
                    : q
            )
        }));
    };

    const validateForm = () => {
        let isValid = true;
        const newQuestionErrors = quiz.questions.map(q => ({
            questionSentence: false,
            options: [],
            noCorrectOption: false
        }));

        if (quiz.name.trim() === '') {
            setErrors(prev => ({ ...prev, name: true }));
            isValid = false;
        } else {
            setErrors(prev => ({ ...prev, name: false }));
        }

        if (quiz.topic.trim() === '') {
            setErrors(prev => ({ ...prev, topic: true }));
            isValid = false;
        } else {
            setErrors(prev => ({ ...prev, topic: false }));
        }

        quiz.questions.forEach((q, index) => {
            if (q.questionSentence.trim() === '') {
                newQuestionErrors[index].questionSentence = true;
                isValid = false;
            }

            if (q.questionTypeId === 1) {
                const optionTextErrors = q.options.map(o => o.text.trim() === '');
                if (optionTextErrors.some(err => err)) {
                    newQuestionErrors[index].options = optionTextErrors;
                    isValid = false;
                }

                if (!q.options.some(opt => opt.isCorrect)) {
                    newQuestionErrors[index].noCorrectOption = true;
                    isValid = false;
                }
            }
        });

        setErrors(prev => ({ ...prev, questions: newQuestionErrors }));
        return isValid;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!validateForm()) {
            alert('Lütfen tüm zorunlu alanları doldurun ve çoktan seçmeli sorular için bir doğru cevap seçin.');
            return;
        }

        try {
            const quizData = {
                name: quiz.name,
                description: quiz.description,
                durationMinutes: quiz.durationMinutes,
                topic: quiz.topic,
                active: quiz.isActive
            };

            const quizResponse = await axiosInstance.post('/quizzes', quizData);
            const quizId = quizResponse.data.id;

            const questionsToAdd = quiz.questions.map((question, i) => {
                const questionData = {
                    number: i + 1,
                    questionSentence: question.questionSentence,
                    questionTypeId: question.questionTypeId,
                    points: question.points || 1,
                    options: []
                };

                if (question.questionTypeId === 1) {
                    questionData.options = question.options.map((option) => ({
                        text: option.text,
                        isCorrect: !!option.isCorrect
                    }));
                }

                console.log('Sending questionData:', questionData); // Debug log
                return questionData;
            });

            await Promise.all(
                questionsToAdd.map(q =>
                    axiosInstance.post(`/quizzes/${quizId}/questions`, q)
                )
            );

            alert(`Quiz ve ${questionsToAdd.length} soru başarıyla oluşturuldu!`);
            navigate('/teacher/my-quizzes');
        } catch (error) {
            console.error('Quiz oluşturma hatası:', error);
            alert(`Quiz oluşturulurken bir hata oluştu: ${error.response?.data?.message || error.message}`);
        }
    };

    return (
        <div className="p-4">
            <h2>Yeni Quiz Oluştur</h2>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: '1rem' }}>
                    <label htmlFor="name">Quiz Adı:</label>
                    <input
                        type="text"
                        id="name"
                        name="name"
                        value={quiz.name}
                        onChange={handleChange}
                    />
                </div>

                <div style={{ marginBottom: '1rem' }}>
                    <label htmlFor="description">Açıklama:</label>
                    <textarea
                        id="description"
                        name="description"
                        value={quiz.description}
                        onChange={handleChange}
                    />
                </div>

                <div style={{ marginBottom: '1rem' }}>
                    <label htmlFor="topic">Konu:</label>
                    <input
                        type="text"
                        id="topic"
                        name="topic"
                        value={quiz.topic}
                        onChange={handleChange}
                    />
                </div>

                <div style={{ marginBottom: '1rem' }}>
                    <label htmlFor="durationMinutes">Süre (dakika):</label>
                    <input
                        type="number"
                        id="durationMinutes"
                        name="durationMinutes"
                        value={quiz.durationMinutes}
                        onChange={handleChange}
                        min="1"
                    />
                </div>

                <h3>Sorular</h3>
                {quiz.questions.map((q, qIndex) => (
                    <div key={qIndex} style={{ border: '1px solid #ccc', padding: '1rem', marginBottom: '1rem' }}>
                        <h4>Soru {qIndex + 1}</h4>
                        <div>
                            <label>Soru Metni:</label>
                            <textarea
                                value={q.questionSentence}
                                onChange={(e) => handleQuestionChange(qIndex, 'questionSentence', e.target.value)}
                                rows="3"
                                style={{
                                    width: '100%',
                                    minHeight: '60px',
                                    borderColor: errors.questions[qIndex]?.questionSentence ? 'red' : ''
                                }}
                            />
                            {errors.questions[qIndex]?.questionSentence && <p className="text-red-600 text-xs">Soru metni zorunludur.</p>}
                        </div>
                        <div>
                            <label>Soru Tipi:</label>
                            <select
                                value={q.questionTypeId}
                                onChange={(e) => handleQuestionChange(qIndex, 'questionTypeId', parseInt(e.target.value))}
                            >
                                {QUESTION_TYPES.map(type => (
                                    <option key={type.value} value={type.value}>{type.label}</option>
                                ))}
                            </select>
                        </div>
                        <div>
                            <label>Puan:</label>
                            <input
                                type="number"
                                value={q.points}
                                onChange={(e) => handleQuestionChange(qIndex, 'points', parseInt(e.target.value) || 0)}
                                min="0"
                            />
                        </div>

                        {q.questionTypeId === 1 && (
                            <div>
                                <p>Seçenekler:</p>
                                {errors.questions[qIndex]?.noCorrectOption && <p className="text-red-600 text-xs">Lütfen bir doğru cevap seçin.</p>}
                                {q.options.map((opt, oIndex) => (
                                    <div key={oIndex} style={{ display: 'flex', alignItems: 'center', marginBottom: '0.5rem' }}>
                                        <input
                                            type="text"
                                            value={opt.text}
                                            onChange={(e) => handleOptionChange(qIndex, oIndex, 'text', e.target.value)}
                                            placeholder={`Seçenek ${oIndex + 1}`}
                                            style={{ borderColor: errors.questions[qIndex]?.options?.[oIndex] ? 'red' : '', marginRight: '0.5rem' }}
                                        />
                                        <input
                                            type="radio"
                                            name={`correctAnswer-${qIndex}`}
                                            checked={opt.isCorrect}
                                            onChange={() => setCorrectAnswer(qIndex, oIndex)}
                                        />
                                        <label style={{ marginLeft: '0.25rem' }}>Doğru</label>
                                        {errors.questions[qIndex]?.options?.[oIndex] && <p className="text-red-600 text-xs ml-2">Seçenek metni zorunludur.</p>}
                                    </div>
                                ))}
                            </div>
                        )}

                        {q.questionTypeId === 2 && (
                            <div className="mt-2 p-2 bg-blue-50 rounded">
                                <p className="text-sm text-blue-700">Açık uçlu soru - Öğrenciler kendi cevaplarını yazacaklar</p>
                            </div>
                        )}

                        <button type="button" onClick={() => removeQuestion(qIndex)}>Soruyu Sil</button>
                    </div>
                ))}

                <button type="button" onClick={addQuestion}>Soru Ekle</button>
                <br />
                <button type="submit">Quizi Oluştur</button>
            </form>
        </div>
    );
}

export default TeacherCreateQuizPage;
