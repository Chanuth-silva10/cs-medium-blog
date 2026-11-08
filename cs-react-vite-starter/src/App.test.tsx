import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import App from './App';

describe('App', () => {
  it('starts at zero', () => {
    render(<App />);
    expect(screen.getByTestId('count')).toHaveTextContent('0');
  });

  it('increments and decrements', async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Increment' }));
    await user.click(screen.getByRole('button', { name: 'Increment' }));
    expect(screen.getByTestId('count')).toHaveTextContent('2');

    await user.click(screen.getByRole('button', { name: 'Decrement' }));
    expect(screen.getByTestId('count')).toHaveTextContent('1');
  });

  it('can go below zero', async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole('button', { name: 'Decrement' }));
    expect(screen.getByTestId('count')).toHaveTextContent('-1');
  });

  it('resets to zero, and the reset button is disabled at zero', async () => {
    const user = userEvent.setup();
    render(<App />);
    const reset = screen.getByRole('button', { name: 'Reset' });
    expect(reset).toBeDisabled();

    await user.click(screen.getByRole('button', { name: 'Increment' }));
    expect(reset).toBeEnabled();
    await user.click(reset);
    expect(screen.getByTestId('count')).toHaveTextContent('0');
  });
});
