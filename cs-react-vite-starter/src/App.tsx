import { useState } from 'react';

const App = () => {
  const [count, setCount] = useState<number>(0);

  return (
    <main>
      <h1>DevCraft counter</h1>
      <h2>
        Number: <b data-testid="count">{count}</b>
      </h2>
      <div className="actions">
        <button type="button" onClick={() => setCount((current) => current + 1)}>
          Increment
        </button>
        <button type="button" onClick={() => setCount((current) => current - 1)}>
          Decrement
        </button>
        <button type="button" onClick={() => setCount(0)} disabled={count === 0}>
          Reset
        </button>
      </div>
    </main>
  );
};

export default App;


